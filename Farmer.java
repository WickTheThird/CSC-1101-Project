import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Farmer extends Thread {
    private final Farm farm;
    private final String farmerName;
    private final WorldState worldState = WorldState.getInstance();

    public Farmer(Farm farm, String farmerName) {
        this.farm = farm;
        this.farmerName = farmerName;
    }

    @Override
    public void run() {
        while (true) {
            try {
                worldState.updateFarmerActivity(farmerName, "Waiting at enclosure");
                var animals = farm.takeFromEnclosure(10); // -> MAKE THIS MORE FLEXIBLE
                
                System.out.println(farmerName + " took " + animals.size() + " animals from the enclosure.");
                worldState.updateFarmerActivity(farmerName, "Collected " + animals.size() + " animals from enclosure");

                stockAnimals(animals);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void stockAnimals(List<String> animals) throws InterruptedException {
        Map<String, Integer> animalCounts = countAnimalsByType(animals);
        
        for (String animalType : animalCounts.keySet()) {
            Field chosenField = findSuitableField(animalType);
            if (chosenField != null) {
                int count = animalCounts.get(animalType);
                
                // Update WorldState - walking to field
                worldState.updateFarmerActivity(farmerName, "Walking to " + chosenField.getName() + " with " + count + " " + animalType);
                Thread.sleep(10); // Simulate walking time 
                
                // Stocking field
                worldState.updateFieldState(chosenField.getName(), chosenField.getCurrentCount(), true);
                worldState.updateFarmerActivity(farmerName, "Stocking " + count + " " + animalType + " into " + chosenField.getName());
                
                System.out.println(farmerName + " is stocking " + count + " " + animalType + " into " + chosenField.getName());
                chosenField.addAnimals(count);
                
                // Update WorldState - field stocked
                worldState.updateFieldState(chosenField.getName(), chosenField.getCurrentCount(), false);
            }
        }
        
        // Return to enclosure
        worldState.updateFarmerActivity(farmerName, "Returning to enclosure");
        Thread.sleep(10); // Walking back
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
