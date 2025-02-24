import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class DeliveryManager extends Thread{
    private final Farm farm;
    private final Random random = new Random();

    public DeliveryManager(Farm farm) {
        this.farm = farm;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);

                if (random.nextInt(100) == 0) {
                    List<String> animals = generateDelivery();
                    farm.addToEnclosure(animals);
                    System.out.println("Delivery manager added " + animals.size() + " animals to the enclosure.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private List<String> generateDelivery() {
        List<String> animals = new ArrayList<>();
        int totalAnimals = 10;
        String[] animalTypes = {"pigs", "cows", "sheep", "llamas", "chickens"};

        for (int i = 0; i < totalAnimals; i++) {
            String animal = animalTypes[random.nextInt(animalTypes.length)];
            animals.add(animal);
        }

        return animals;
    }
}