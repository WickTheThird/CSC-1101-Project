import java.util.List;
import java.util.Random;

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

                if (waitedTicks >= MAX_WAIT_TIME) {
                    String previousField = (currentField != null) ? currentField.getName() : "none";
                    FarmLogger.logBuyerGaveUp(buyerName, previousField, waitedTicks);

                    if (currentField != null) {
                        worldState.removeWaitingBuyer(currentField.getName());
                    }

                    waitedTicks = 0;
                    currentField = null;
                }

                Field field;
                if (currentField == null) {
                    field = getRandomField();
                    if (field == null) continue;
                    currentField = field;
                } else {
                    field = currentField;
                }

                synchronized (field) {
                    if (field.isBeingStocked()) {
                        waitedTicks++;
                        worldState.updateBuyerActivity(buyerName, "Waiting - " + field.getName() + 
                                                    " is being stocked (" + waitedTicks + "/" + MAX_WAIT_TIME + ")");
                        worldState.addWaitingBuyer(field.getName());
                        
                        FarmLogger.logBuyerWaiting(buyerName, field.getName(), "being_stocked");
                        continue;
                    }

                    if (field.tryRemoveAnimal()) {
                        int waited = waitedTicks;
                        waitedTicks = 0;
                        worldState.removeWaitingBuyer(field.getName());
                        currentField = null;
                        
                        String animalType = field.getName();
                        worldState.updateBuyerActivity(buyerName, "Bought a " + animalType + " animal");
                        
                        FarmLogger.logBuyerCollection(buyerName, field.getName(), waited);
                    } else {
                        waitedTicks++;
                        String animalType = field.getName();
                        worldState.updateBuyerActivity(buyerName, "Waiting for " + animalType + 
                                                    " (" + waitedTicks + "/" + MAX_WAIT_TIME + ")");
                        worldState.addWaitingBuyer(field.getName());
                        
                        FarmLogger.logBuyerWaiting(buyerName, field.getName(), "empty");
                    }
                }

                waitForTicks(random.nextInt(5) + 1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Field getRandomField() {
        List<Field> fields = farm.getFields();
        if (fields.isEmpty()) return null;
        return fields.get(random.nextInt(fields.size()));
    }

    private void waitForTicks(int ticks) throws InterruptedException {
        if (ticks <= 0) return;
        
        int targetTick = tickManager.getCurrentTick() + ticks;
        
        while (tickManager.getCurrentTick() < targetTick) {
            waitForNextTick();
        }
    }

    private void waitForNextTick() throws InterruptedException {
        synchronized (tickManager) {
            int currentTick = tickManager.getCurrentTick();
            if (lastCheckedTick == currentTick) {
                tickManager.wait(); // Wait for tick to change
            }
            lastCheckedTick = tickManager.getCurrentTick();
        }
    }
}
