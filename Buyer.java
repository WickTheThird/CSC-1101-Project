import java.util.List;
import java.util.Random;

// Represents a buyer in the farm simulation.
// A Buyer will attempt to buy animals from fields, wait if the field is empty
// or being stocked, and eventually give up if the wait time exceeds a threshold.
class Buyer extends Thread {
    private final Farm farm;
    private final String buyerName;
    private final WorldState worldState = WorldState.getInstance();
    private final TickManager tickManager;
    private int lastCheckedTick = 0;
    private final Random random = new Random();
    private int waitedTicks = 0;
    private Field currentField = null;
    private static final int MAX_WAIT_TIME = 50;

    // Constructor to initialize the Buyer
    public Buyer(String buyerName, Farm farm, TickManager tickManager) {
        this.buyerName = buyerName;
        this.farm = farm;
        this.tickManager = tickManager;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                waitForNextTick();

                // Increment wait counter if already waiting for a field
                if (currentField != null) {
                    waitedTicks++;
                }

                // If waited too long, give up and reset state
                if (waitedTicks >= MAX_WAIT_TIME) {
                    String previousField = (currentField != null) ? currentField.getName() : "none";
                    FarmLogger.logBuyerGaveUp(buyerName, previousField, waitedTicks);

                    if (currentField != null) {
                        worldState.removeWaitingBuyer(currentField.getName());
                    }

                    waitedTicks = 0;
                    currentField = null;
                    continue; // Start fresh next tick
                }

                // Get or use current field
                Field field;
                if (currentField == null) {
                    field = getRandomField();
                    if (field == null) continue;
                    currentField = field;
                    // Reset wait counter when selecting a new field
                    waitedTicks = 0;
                } else {
                    field = currentField;
                }

                // Check if field is being stocked
                if (field.isBeingStocked()) {
                    worldState.updateBuyerActivity(buyerName, "Waiting - " + field.getName() + 
                                    " is being stocked (" + waitedTicks + "/" + MAX_WAIT_TIME + ")");
                    worldState.addWaitingBuyer(field.getName());
                    FarmLogger.logBuyerWaiting(buyerName, field.getName(), "being_stocked");
                    continue;
                }

                // Try to buy an animal
                if (field.tryRemoveAnimal()) {
                    // Successfully bought an animal
                    int waited = waitedTicks;
                    worldState.removeWaitingBuyer(field.getName());
                    String animalType = field.getName();
                    worldState.updateBuyerActivity(buyerName, "Bought a " + animalType + " animal");
                    FarmLogger.logBuyerCollection(buyerName, field.getName(), waited);
                    
                    // Reset for next purchase
                    currentField = null;
                    waitedTicks = 0;
                    
                    // Wait for a random number of ticks before next attempt
                    waitForTicks(random.nextInt(5) + 1);
                } else {
                    // No animal available
                    worldState.updateBuyerActivity(buyerName, "Waiting for " + field.getName() + 
                                    " (" + waitedTicks + "/" + MAX_WAIT_TIME + ")");
                    worldState.addWaitingBuyer(field.getName());
                    FarmLogger.logBuyerWaiting(buyerName, field.getName(), "empty");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Gets a random field from the farm.
    private Field getRandomField() {
        List<Field> fields = farm.getFields();
        if (fields.isEmpty()) return null;
        return fields.get(random.nextInt(fields.size()));
    }

    // Wait for a specified number of ticks.
    private void waitForTicks(int ticks) throws InterruptedException {
        if (ticks <= 0) return;

        int targetTick = tickManager.getCurrentTick() + ticks;

        while (tickManager.getCurrentTick() < targetTick) {
            waitForNextTick();
        }
    }

    // Wait for the next tick from the TickManager.
    private void waitForNextTick() throws InterruptedException {
        lastCheckedTick = tickManager.waitForNextTick(lastCheckedTick);
    }
}
