import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the periodic delivery of animals to the farm.
 * Runs as a separate thread and delivers random animals based on configuration settings.
 */
class DeliveryManager extends Thread {
    private final Farm farm;
    private final Random random = new Random();
    private final TickManager tickManager; // Manages the timing system
    private int lastCheckedTick = 0;
    private static final Random staticRandom = new Random();

    public DeliveryManager(Farm farm, TickManager tickManager) {
        this.farm = farm;
        this.tickManager = tickManager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Wait for the next tick before processing
                lastCheckedTick = tickManager.waitForNextTick(lastCheckedTick);
                
                // Randomly determine if a delivery should happen this tick
                if (random.nextInt(Config.DELIVERY_FREQUENCY) == 0) {
                    List<String> animals = generateDelivery();
                    
                    // First log the delivery event
                    FarmLogger.logDelivery(FarmLogger.formatDelivery(animals));
                    
                    // Add the animals to the farm enclosure
                    farm.addToEnclosure(animals);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // Creates a randomized list of animals to deliver to the farm
    public static List<String> generateDelivery() {
        List<String> animals = new ArrayList<>();
        int totalAnimals = Config.DELIVERY_SIZE;
        String[] animalTypes = {"pigs", "cows", "sheep", "llamas", "chickens"};

        // Randomly select animals up to the delivery size specified in Config.java
        for (int i = 0; i < totalAnimals; i++) {
            String animal = animalTypes[staticRandom.nextInt(animalTypes.length)];
            animals.add(animal);
        }

        return animals;
    }
}
