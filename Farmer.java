
class Farmer extends Thread {
    private final Farm farm;
    private final String farmerName;

    public Farmer(Farm farm, String farmerName) {
        this.farm = farm;
        this.farmerName = farmerName;
    }

    @Override
    public void run() {
        while (true) {
            try {
                var animals = farm.takeFromEnclosure(10); // -> MAKE THIS MORE FLEXIBLE
                System.out.println(farmerName + " took " + animals.size() + " animals from the enclosure.");

                stockAnimals(animals);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void stockAnimals(java.util.List<String> animals) throws InterruptedException {
        for (String animal : animals) {
            Field chosenField = findSuitableField(animal);
            if (chosenField != null) {
                System.out.println(farmerName + " is stocking 1 " + animal + " into " + chosenField.getName());

                // Simulate walking to field, etc.
                chosenField.addAnimals(1);
            }
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
}
