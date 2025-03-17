import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Represents a farmer in the farm simulation.
// A Farmer will collect animals from the enclosure and stock them into fields.
class Farmer extends Thread {
    private final Farm farm;
    private final String farmerName;
    private final WorldState worldState = WorldState.getInstance();
    private final TickManager tickManager;
    private int lastCheckedTick = 0;
    private final Random random = new Random();
    private boolean onBreak = false;
    private int breakCounter = 0;

    // Constructor to initialize the Farmer
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

                // 5% chance the farmer decides to take a break
                if (!onBreak && random.nextInt(100) < Config.FARMER_BREAK_CHANCE) {
                    onBreak = true;
                    breakCounter = Config.FARMER_BREAK_MIN_DURATION + random.nextInt(Config.FARMER_BREAK_MAX_DURATION - Config.FARMER_BREAK_MIN_DURATION + 1);
                    worldState.updateFarmerActivity(farmerName, "On break for " + breakCounter + " ticks");
                    FarmLogger.logFarmerBreak(farmerName, breakCounter);
                    continue;
                }

                // If on break, decrement the break counter
                if (onBreak) {
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

                // Update status while waiting at enclosure
                worldState.updateFarmerActivity(farmerName, "Waiting at enclosure");

                if (farm.hasAnimalsInEnclosure()) {
                    // Take animals from enclosure
                    List<String> animals = farm.takeFromEnclosure(Config.FARMER_MAX_ANIMALS);
                    
                    if (!animals.isEmpty()) {
                        FarmLogger.logFarmerCollection(farmerName, animals.size());
                        stockAnimals(animals);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Wait for the next simulation tick
    private void waitForNextTick() throws InterruptedException {
        lastCheckedTick = tickManager.waitForNextTick(lastCheckedTick);
    }

    // Stock animals into fields
    private void stockAnimals(List<String> animals) throws InterruptedException {
        // Group animals by type
        Map<String, Integer> animalCounts = countAnimalsByType(animals);

        // Sort fields by priority using the extracted method
        List<Map.Entry<String, Integer>> sortedAnimals = getSortedAnimals(animalCounts);

        // Current location of the farmer
        String currentLocation = "enclosure";

        for (Map.Entry<String, Integer> entry : sortedAnimals) {
            String animalType = entry.getKey();
            int count = entry.getValue();

            Field field = findSuitableField(animalType);
            if (field == null) continue;

            // Calculate movement time
            int movementTime = 10 + count; // Base time + 1 per animal

            // Update farmer activity - moving to field
            worldState.updateFarmerActivity(farmerName, "Moving to " + field.getName() + " with " + count + " animals");
            FarmLogger.logFarmerMoving(farmerName, field.getName(), movementTime, count);
            waitForTicks(movementTime);

            try {
                field.startStocking();

                // Update farmer activity - stocking field
                worldState.updateFarmerActivity(farmerName, "Stocking " + field.getName() + " with " + count + " animals");
                FarmLogger.logFarmerBeginStocking(farmerName, field.getName(), count);

                int toStock = Math.min(count, field.getCapacity() - field.getCurrentCount());
                waitForTicks(toStock);

                field.addAnimals(toStock);

                worldState.updateFarmerActivity(farmerName, "Finished stocking " + field.getName());
                FarmLogger.logFarmerFinishStocking(farmerName, field.getName(), toStock);
                currentLocation = field.getName();
            } finally {
                field.finishStocking();
            }
        }

        // Return to enclosure if not already there
        if (!currentLocation.equals("enclosure")) {
            worldState.updateFarmerActivity(farmerName, "Returning to enclosure");
            int returnTime = 10;
            FarmLogger.logFarmerReturning(farmerName, returnTime);
            waitForTicks(returnTime);
        }
    }

    // Wait for the specified number of ticks
    private void waitForTicks(int ticksToWait) throws InterruptedException {
        int targetTick = lastCheckedTick + ticksToWait;
        while (lastCheckedTick < targetTick) {
            waitForNextTick();
        }
    }

    // Find a suitable field for the given animal type
    private Field findSuitableField(String animal) {
        for (Field f : farm.getFields()) {
            if (f.getName().equalsIgnoreCase(animal)) {
                return f;
            }
        }
        return null;
    }

    // Count the number of animals by type
    private Map<String, Integer> countAnimalsByType(List<String> animals) {
        Map<String, Integer> counts = new HashMap<>();
        for (String animal : animals) {
            counts.put(animal, counts.getOrDefault(animal, 0) + 1);
        }
        return counts;
    }

    private List<Map.Entry<String, Integer>> getSortedAnimals(Map<String, Integer> animalCounts) {
        List<Map.Entry<String, Integer>> sortedAnimals = new ArrayList<>(animalCounts.entrySet());
        sortedAnimals.sort((a, b) -> {
            Field fieldA = findSuitableField(a.getKey());
            Field fieldB = findSuitableField(b.getKey());

            // Priority: fields with waiting buyers
            boolean aHasWaiting = worldState.hasWaitingBuyers(a.getKey());
            boolean bHasWaiting = worldState.hasWaitingBuyers(b.getKey());

            if (aHasWaiting && !bHasWaiting) return -1;
            if (!aHasWaiting && bHasWaiting) return 1;

            // Priority: fields with the lowest stock ratio
            if (fieldA != null && fieldB != null) {
                double aRatio = (double) fieldA.getCurrentCount() / fieldA.getCapacity();
                double bRatio = (double) fieldB.getCurrentCount() / fieldB.getCapacity();
                return Double.compare(aRatio, bRatio);
            }

            return 0;
        });
        return sortedAnimals;
    }
}
