import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class DeliveryManager extends Thread{
    private final Farm farm;
    private final Random random = new Random();
    private final TickManager tickManager;
    private int lastCheckedTick = 0;

    public DeliveryManager(Farm farm, TickManager tickManager) {
        this.farm = farm;
        this.tickManager = tickManager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                waitForNextTick();

                if (random.nextInt(Config.DELIVERY_FREQUENCY) == 0) {
                    List<String> animals = generateDelivery();
                    farm.addToEnclosure(animals);
                    System.out.println(tickManager.getCurrentTick() + "Delivery manager added " + animals.size() + " animals to the enclosure.");
                }
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

    private List<String> generateDelivery() {
        List<String> animals = new ArrayList<>();
        int totalAnimals = Config.DELIVERY_SIZE;
        String[] animalTypes = {"pigs", "cows", "sheep", "llamas", "chickens"};

        for (int i = 0; i < totalAnimals; i++) {
            String animal = animalTypes[random.nextInt(animalTypes.length)];
            animals.add(animal);
        }

        return animals;
    }
}
