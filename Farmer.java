import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class Farmer extends Thread {
    private final Farm farm;
    private final String farmerName;
    private final WorldState worldState = WorldState.getInstance();
    private final TickManager tickManager;
    private int lastCheckedTick = 0;
    private final Random random = new Random();
    private boolean onBreak = false;
    private int breakCounter = 0;


    public Farmer(Farm farm, String farmerName, TickManager tickManager) {
        this.farm = farm;
        this.farmerName = farmerName;
        this.tickManager = tickManager;
    }

 @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                // Wait for a tick
                 waitForNextTick();

                // Check if farmer should take a break
                if (!onBreak && random.nextInt(100) < 5) { // 5% chance of taking a break
                    onBreak = true;
                    breakCounter = 20 + random.nextInt(20); // Break for 20-40 ticks
                    worldState.updateFarmerActivity(farmerName, "On break for " + breakCounter + " ticks");
                    FarmLogger.logFarmerBreak(farmerName, breakCounter);
                    continue;
                }

                // If on break, decrement counter
                if (onBreak) {
                    // Update status each tick while on break for better visibility
                    worldState.updateFarmerActivity(farmerName, "On break for " + breakCounter + " more ticks");

                    breakCounter--;
                    if (breakCounter <= 0) {
                        onBreak = false;
                        worldState.updateFarmerActivity(farmerName, "Returning from break");
                        FarmLogger.logFarmerBreakEnded(farmerName);
                    } else {
                        continue; // Skip the rest of the loop if still on break
                    }
                }
                
                // Update status
                worldState.updateFarmerActivity(farmerName, "Waiting at enclosure");
                
                // Add delay to let GUI display animals in enclosure
                try {
                    Thread.sleep(300); // Wait 300ms before taking animals
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                // Take animals from enclosure
                List<String> animals = farm.takeFromEnclosure(10);
                
                if (!animals.isEmpty()) {
                    FarmLogger.logFarmerCollection(farmerName, animals.size());
                    stockAnimals(animals);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForNextTick() throws InterruptedException {
        synchronized (tickManager) {
            int currentTick = tickManager.getCurrentTick();
            if (lastCheckedTick == currentTick) {
                tickManager.wait();
            }
            lastCheckedTick = currentTick;
        }
    }

    private void stockAnimals(List<String> animals) throws InterruptedException {
        // Group animals by type
        Map<String, Integer> animalCounts = countAnimalsByType(animals);
        
        // Sort fields by priority (fields with waiting buyers first, then fields with lowest stock)
        List<Map.Entry<String, Integer>> sortedAnimals = new ArrayList<>(animalCounts.entrySet());
        sortedAnimals.sort((a, b) -> {
            Field fieldA = findSuitableField(a.getKey());
            Field fieldB = findSuitableField(b.getKey());
            
            // First priority: fields with waiting buyers
            boolean aHasWaiting = worldState.hasWaitingBuyers(a.getKey());
            boolean bHasWaiting = worldState.hasWaitingBuyers(b.getKey());
            
            if (aHasWaiting && !bHasWaiting) return -1;
            if (!aHasWaiting && bHasWaiting) return 1;
            
            // Second priority: fields with lowest stock relative to capacity
            if (fieldA != null && fieldB != null) {
                double aRatio = (double) fieldA.getCurrentCount() / fieldA.getCapacity();
                double bRatio = (double) fieldB.getCurrentCount() / fieldB.getCapacity();
                return Double.compare(aRatio, bRatio);
            }
            
            return 0;
        });
        
        // Current location tracker
        String currentLocation = "enclosure";
        
        // Process animals in priority order
        for (Map.Entry<String, Integer> entry : sortedAnimals) {
            String animalType = entry.getKey();
            int count = entry.getValue();
            
            Field field = findSuitableField(animalType);
            if (field == null) continue;
            
            // Calculate movement time
            int movementTime = 0;
            if (currentLocation.equals("enclosure")) {
                // From enclosure to field: 10 ticks + 1 per animal
                movementTime = 10 + count;
            } else {
                // From one field to another: 10 ticks + 1 per animal
                movementTime = 10 + count;
            }
            
            // Update farmer activity - moving to field
            worldState.updateFarmerActivity(farmerName, "Moving to " + field.getName() + " with " + count + " animals");
            
            // Wait for movement time
            FarmLogger.logFarmerMoving(farmerName, field.getName(), movementTime, count);
            waitForTicks(movementTime);
            
            // Try to stock the field
            try {
                field.startStocking();
                
                // Update farmer activity - stocking field
                worldState.updateFarmerActivity(farmerName, "Stocking " + field.getName() + " with " + count + " animals");
                
                FarmLogger.logFarmerBeginStocking(farmerName, field.getName(), count);
                
                // Determine how many can actually be stocked
                int toStock = Math.min(count, field.getCapacity() - field.getCurrentCount());
                
                // Wait 1 tick per animal stocked
                waitForTicks(toStock);
                
                // Stock the animals
                field.addAnimals(toStock);
                
                // Update farmer activity - finished stocking
                worldState.updateFarmerActivity(farmerName, "Finished stocking " + field.getName());
                
                FarmLogger.logFarmerFinishStocking(farmerName, field.getName(), toStock);
                
                // Update current location
                currentLocation = field.getName();
            } finally {
                field.finishStocking();
            }
        }
        
        // Return to enclosure if not already there
        if (!currentLocation.equals("enclosure")) {
            // Update farmer activity - returning to enclosure
            worldState.updateFarmerActivity(farmerName, "Returning to enclosure");
            
            int returnTime = 10; // Base return time
            FarmLogger.logFarmerReturning(farmerName, returnTime);
            waitForTicks(returnTime);
        }
    }

    private void waitForTicks(int ticksToWait) throws InterruptedException {
        int targetTick = lastCheckedTick + ticksToWait;
        
        while (lastCheckedTick < targetTick) {
            waitForNextTick();
        }
    }

    private Field findSuitableField(String animal) {
        // Simple approach: find matching field by name or any field with capacity
        for (Field f : farm.getFields()) {
            if (f.getName().equalsIgnoreCase(animal)) {
                return f;
            }
        }
        return null;
    }
    
    // Helper method to count animals by type
    private Map<String, Integer> countAnimalsByType(List<String> animals) {
        Map<String, Integer> counts = new HashMap<>();
        for (String animal : animals) {
            counts.put(animal, counts.getOrDefault(animal, 0) + 1);
        }
        return counts;
    }
}
