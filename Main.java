class Main {
    public static void main(String[] args) {
        // Initialize world state
        WorldState worldState = WorldState.getInstance();

        // Create and set up the farm
        Farm farm = new Farm();
        farm.addField();

        // Initialize tick manager
        int tickSize = Config.TICK_SIZE;
        TickManager tickManager = new TickManager(tickSize);
        FarmLogger.setTickManager(tickManager);

        // Check if GUI should be displayed
        boolean showGUI = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-gui") || arg.equalsIgnoreCase("--gui")) {
                showGUI = true;
                System.out.println("GUI will be displayed");
                break;
            }
        }

        // Only create and set up GUI if requested
        if (showGUI) {
            try {
                FarmGUI gui = new FarmGUI(tickManager, farm);
                worldState.setGUI(gui);
                System.out.println("GUI initialized successfully");
            } catch (Exception e) {
                System.err.println("Failed to initialize GUI: " + e.getMessage());
                System.out.println("Continuing in headless mode");
            }
        }

        // Start the tick manager
        tickManager.start();

        // Start the delivery manager
        DeliveryManager deliveryManager = new DeliveryManager(farm, tickManager);
        deliveryManager.start();

        // Create and start farmers
        int numberOfFarmers = Config.NUMBER_OF_FARMERS;
        for (int i = 0; i < numberOfFarmers; i++) {
            Farmer farmer = new Farmer(farm, String.valueOf(i + 1), tickManager);
            farmer.start();
        }

        // Create and start buyers
        int numberOfBuyers = Config.NUMBER_OF_BUYERS;
        for (int i = 0; i < numberOfBuyers; i++) {
            Buyer buyer = new Buyer(String.valueOf(i + 1), farm, tickManager);
            buyer.start();
        }

        // Let the simulation run for the specified duration
        try {
            Thread.sleep((long) Config.SIMULATION_DURATION * tickSize);
        } catch (InterruptedException e) {
            // Restore interrupt status and exit
            Thread.currentThread().interrupt();
        }

        // Stop the tick manager
        tickManager.stopTicks();

        // Notify GUI if simulation ended
        if (showGUI && worldState.getGUI() != null) {
            worldState.getGUI().showSimulationEnded();
        }
    }
}
