import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldState {
    private static final String LOG_FILE_PATH = "farm_simulation_log.txt";
    
    // Maps to track activities of farmers and buyers
    private final Map<String, String> farmerActivities = new ConcurrentHashMap<>();
    private final Map<String, String> buyerActivities = new ConcurrentHashMap<>();
    
    // Track field states (animal counts and being stocked status)
    private final Map<String, FieldState> fieldStates = new ConcurrentHashMap<>();
    
    // Track enclosure state
    private final Map<String, Integer> enclosureState = new ConcurrentHashMap<>();
    
    // Current tick
    private volatile int currentTick = 0;
    
    // Singleton instance
    private static WorldState instance;
    
    // Constructor
    private WorldState() {
        // Initialize log file
        try {
            Files.write(Paths.get(LOG_FILE_PATH), 
                       ("Farm Simulation Log - Started at " + 
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n").getBytes(),
                       StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error initializing log file: " + e.getMessage());
        }
        
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
    
    // Update the current tick and log the state
    public synchronized void updateTick(int tick) {
        this.currentTick = tick;
        logState();
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
    
    // Update enclosure state with counts of each animal type
    public void updateEnclosureAnimals(Map<String, Integer> animalCounts) {
        synchronized(enclosureState) {
            enclosureState.clear();
            enclosureState.putAll(animalCounts);
        }
    }
    
    // Add animals to enclosure
    public void addAnimalsToEnclosure(List<String> animals) {
        synchronized(enclosureState) {
            for (String animal : animals) {
                enclosureState.put(animal, enclosureState.getOrDefault(animal, 0) + 1);
            }
        }
    }
    
    // Remove animals from enclosure
    public void removeAnimalsFromEnclosure(List<String> animals) {
        synchronized(enclosureState) {
            for (String animal : animals) {
                int currentCount = enclosureState.getOrDefault(animal, 0);
                if (currentCount > 0) {
                    enclosureState.put(animal, currentCount - 1);
                }
            }
        }
    }
    
    // Log the current state to file
    private void logState() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== TICK ").append(currentTick).append(" ===\n");
        
        // Log enclosure state
        sb.append("Enclosure: ");
        for (Map.Entry<String, Integer> entry : enclosureState.entrySet()) {
            if (entry.getValue() > 0) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
            }
        }
        sb.append("\n");
        
        // Log field states
        sb.append("Fields:\n");
        for (Map.Entry<String, FieldState> entry : fieldStates.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ")
              .append(entry.getValue()).append("\n");
        }
        
        // Log farmer states
        sb.append("Farmers:\n");
        for (Map.Entry<String, String> entry : farmerActivities.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ")
              .append(entry.getValue()).append("\n");
        }
        
        // Log buyer states
        sb.append("Buyers:\n");
        for (Map.Entry<String, String> entry : buyerActivities.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ")
              .append(entry.getValue()).append("\n");
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
    
    // Inner class to represent field state
    public static class FieldState {
        private final int animalCount;
        private final boolean isBeingStocked;
        
        public FieldState(int animalCount, boolean isBeingStocked) {
            this.animalCount = animalCount;
            this.isBeingStocked = isBeingStocked;
        }
        
        @Override
        public String toString() {
            return "Animal Count: " + animalCount + 
                   ", Being Stocked: " + isBeingStocked;
        }
    }
}
