import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class DeliveryManager extends Thread {
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
                    
                    // First update the GUI to show delivery
                    FarmLogger.logDelivery(formatDelivery(animals));
                    
                    farm.addToEnclosure(animals);
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

    private String formatDelivery(List<String> animals) {
    Map<String, Integer> counts = new HashMap<>();
    for (String animal : animals) {
        counts.put(animal, counts.getOrDefault(animal, 0) + 1);
    }
    
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    
    List<String> sortedTypes = new ArrayList<>(counts.keySet());
    Collections.sort(sortedTypes);
    
    for (String type : sortedTypes) {
        int count = counts.get(type);
        if (count > 0) {
            if (!first) {
                sb.append(" ");
            }
            sb.append(type).append("=").append(count);
            first = false;
        }
    }
    
    return sb.toString();
    }
}
