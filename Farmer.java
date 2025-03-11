import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Farmer extends Thread {
    private final Farm farm;
    private final String farmerName;
    private final WorldState worldState = WorldState.getInstance();
    private final TickManager tickManager;
    private int lastCheckedTick = 0;

    public Farmer(Farm farm, String farmerName, TickManager tickManager) {
        this.farm = farm;
        this.farmerName = farmerName;
        this.tickManager = tickManager;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                worldState.updateFarmerActivity(farmerName, "Waiting at enclosure");
                var animals = farm.takeFromEnclosure(10); // -> MAKE THIS MORE FLEXIBLE
                
                if (!animals.isEmpty()) {
                    System.out.println(farmerName + " took " + animals.size() + " animals from the enclosure.");
                    worldState.updateFarmerActivity(farmerName, "Collected " + animals.size() + " animals from enclosure");
                    stockAnimals(animals);
                }
                
                // Wait for the next tick
                waitForNextTick();
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
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
        Map<String, Integer> animalCounts = countAnimalsByType(animals);
        int totalRemainingAnimals = animals.size();
        
        for (Map.Entry<String, Integer> entry : animalCounts.entrySet()) {
            String animalType = entry.getKey();
            int count = entry.getValue();
            Field chosenField = findSuitableField(animalType);
            
            if (chosenField != null) {
                try {
                    worldState.updateFarmerActivity(farmerName, "Waiting to stock " + chosenField.getName());
                    chosenField.startStocking();
                    
                    // Travel to field
                    worldState.updateFarmerActivity(farmerName, "Walking to " + chosenField.getName());
                    waitForTicks(10 + totalRemainingAnimals);
                    totalRemainingAnimals -= count;
                    
                    // Stock the animals
                    worldState.updateFieldState(chosenField.getName(), chosenField.getCurrentCount(), true);
                    worldState.updateFarmerActivity(farmerName, "Stocking " + count + " " + animalType);
                    
                    for (int i = 0; i < count; i++) {
                        chosenField.addAnimals(1);
                        worldState.updateFieldState(chosenField.getName(), chosenField.getCurrentCount(), true);
                        waitForTicks(1);
                    }
                    
                    worldState.updateFieldState(chosenField.getName(), chosenField.getCurrentCount(), false);
                } finally {
                    chosenField.finishStocking();
                }
            }
        }
        
        worldState.updateFarmerActivity(farmerName, "Returning to enclosure");
        waitForTicks(10);
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
