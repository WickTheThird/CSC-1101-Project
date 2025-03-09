class Main {
    public static void main(String[] args) {
        WorldState.getInstance();
        
        Farm farm = new Farm();
        farm.addField();

        int tickSize = 100;
        TickManager tickManager = new TickManager(tickSize);
        tickManager.start();

        DeliveryManager deliveryManager = new DeliveryManager(farm);
        deliveryManager.start();

        int numberOfFarmers = 1;
        for (int i = 0; i < numberOfFarmers; i++) {
            Farmer farmer = new Farmer(farm, "Farmer " + (i + 1));
            farmer.start();
        }

        int numberOfBuyers = 3;
        for (int i = 0; i < numberOfBuyers; i++) {
            Buyer buyer = new Buyer(farm, "Buyer " + (i + 1), tickManager);
            buyer.start();
        }

        try {
            Thread.sleep(10000 * tickSize);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        tickManager.stopTicks();
    }
}
