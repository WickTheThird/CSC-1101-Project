import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

public class WorldState {
    // Maps to track activities of farmers and buyers
    private final Map<String, String> farmerActivities = new ConcurrentHashMap<>(); // Using ConcurrentHashMap helps with performance over a synchronized HashMap or class
    private final Map<String, String> buyerActivities = new ConcurrentHashMap<>();
    private final Map<String, Integer> waitingBuyers = new ConcurrentHashMap<>();
    private final Map<String, FieldState> fieldStates = new ConcurrentHashMap<>(); // Track field states (animal counts and being stocked status)
    private final Map<String, Integer> enclosureState = new ConcurrentHashMap<>(); // Track enclosure state

    private volatile int currentTick = 0; // Current tick
    private FarmGUI gui; // GUI Reference
    private static WorldState instance; // Singleton instance

    // Constructor
    private WorldState() {
        // Initialize log file using FarmLogger
        FarmLogger.initializeLogFile();
        
        // Initialize animal types in enclosure
        enclosureState.put("pigs", 0);
        enclosureState.put("cows", 0);
        enclosureState.put("sheep", 0);
        enclosureState.put("llamas", 0);
        enclosureState.put("chickens", 0);
    }
    
    // Singleton getter
    public static synchronized WorldState getInstance() {
        if (instance == null) {
            instance = new WorldState();
        }
        return instance;
    }
    
    // Set the GUI reference
    public void setGUI(FarmGUI gui) {
        this.gui = gui;
    }

    // Common method for GUI updates
    public void updateGUI() {
        if (gui != null) {
            SwingUtilities.invokeLater(() -> gui.update());
        }
    }
    
    // Update the current tick and log the state
    public synchronized void updateTick(int tick) {
        this.currentTick = tick;
        logState();
        
        // Update GUI if available
        updateGUI();
    }
    
    // Initialize field states
    public void initializeField(String fieldName, int animalCount) {
        fieldStates.put(fieldName, new FieldState(animalCount, false));
    }
    
    // Update farmer activity 
    public void updateFarmerActivity(String farmerName, String activity) {
        farmerActivities.put(farmerName, activity);
    }
    
    // Update buyer activity
    public void updateBuyerActivity(String buyerName, String activity) {
        buyerActivities.put(buyerName, activity);
    }
    
    // Update field state
    public void updateFieldState(String fieldName, int animalCount, boolean isBeingStocked) {
        fieldStates.put(fieldName, new FieldState(animalCount, isBeingStocked));
    }
    
    // Add animals to enclosure
    public void addAnimalsToEnclosure(List<String> animals) {
        if (animals == null || animals.isEmpty()) return;
        
        // Concurrent hash map provides compute method for atomic updates
        for (String animal : animals) {
            enclosureState.compute(animal, (k, v) -> (v == null) ? 1 : v + 1);
        }
        
        updateGUI();
    }

    // Remove animals from enclosure
    public void removeAnimalsFromEnclosure(List<String> animals) {
        if (animals == null || animals.isEmpty()) return;
        // Again, methods provided by ConcurrentHashMap are super useful for maintaining atomicity
        for (String animal : animals) {
            enclosureState.compute(animal, (k, v) -> (v == null || v <= 0) ? 0 : v - 1);
        }
        
        updateGUI();
    }
    
    // Getters for GUI to access data
    public Map<String, String> getFarmerActivities() {
        return Collections.unmodifiableMap(farmerActivities);
    }
    
    public Map<String, String> getBuyerActivities() {
        return Collections.unmodifiableMap(buyerActivities);
    }
    
    public Map<String, FieldState> getFieldStates() {
        return Collections.unmodifiableMap(fieldStates);
    }
    
    public Map<String, Integer> getEnclosureState() {
        // Create a defensive copy to prevent concurrent modification
        synchronized(enclosureState) {
            return new HashMap<>(enclosureState);
        }
    }
    
    public int getCurrentTick() {
        return currentTick;
    }
    
    public FarmGUI getGUI() {
        return gui;
    }
    
    // Log the current state to file
    private void logState() {
        // Use FarmLogger to log the world state inside the external logging file
        FarmLogger.logWorldState(
            currentTick, 
            Collections.unmodifiableMap(enclosureState),
            Collections.unmodifiableMap(fieldStates),
            Collections.unmodifiableMap(farmerActivities),
            Collections.unmodifiableMap(buyerActivities)
        );
    }

    // Inner class to represent field state
    public record FieldState(int animalCount, boolean isBeingStocked) {
        @Override
        public String toString() {
            return "Animal Count: " + animalCount +
                   ", Being Stocked: " + isBeingStocked;
        }
    }

    public void addWaitingBuyer(String fieldName) {
        waitingBuyers.compute(fieldName, (k, v) -> (v == null) ? 1 : v + 1); // Using compute instead of put for its atimic nature

        updateGUI();
    }

    public void removeWaitingBuyer(String fieldName) {
        int currentCount = waitingBuyers.getOrDefault(fieldName, 0);
        if (currentCount > 0) {
            waitingBuyers.put(fieldName, currentCount - 1);
        }

        updateGUI();
    }

    public boolean hasWaitingBuyers(String fieldName) {
        return waitingBuyers.getOrDefault(fieldName, 0) > 0;
    }

    // This method is synchronized to ensure thread safety when multiple threads attempt to update field states concurrently.
    public synchronized void updateFieldCount(String fieldName, int count) {
        FieldState currentState = fieldStates.get(fieldName);
        if (currentState != null) {
            boolean isStocking = currentState.isBeingStocked();
            fieldStates.put(fieldName, new FieldState(count, isStocking));
        } else {
            fieldStates.put(fieldName, new FieldState(count, false));
        }

        // Ensure GUI updates
        updateGUI();
    }
}
