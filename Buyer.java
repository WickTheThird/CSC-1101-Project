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
                // Wait for next tick
                waitForNextTick();
                
                // Get a field - either continue with current field or pick a new one
                if (currentField == null || waitedTicks >= MAX_WAIT_TIME) {
                    // If we've waited too long or don't have a field, pick a new one
                    if (waitedTicks >= MAX_WAIT_TIME) {
                        System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                         " buyer=" + buyerName + " giving_up on " + currentField.getName() + 
                                         " after " + waitedTicks + " ticks");
                        
                        // If we were waiting for the old field, remove ourselves
                        if (currentField != null) {
                            worldState.removeWaitingBuyer(currentField.getName());
                        }
                    }
                    
                    // Reset waiting counter and get new field
                    currentField = getRandomField();
                    waitedTicks = 0;
                    if (currentField == null) continue;
                }
                
                // Try to buy from the current field
                synchronized (currentField) {
                    // Check if field is being stocked
                    if (currentField.isBeingStocked()) {
                        waitedTicks++;
                        worldState.updateBuyerActivity(buyerName, "Waiting for " + currentField.getName() + " (stocking)");
                        worldState.addWaitingBuyer(currentField.getName());
                        
                        System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                        " buyer=" + buyerName + " waiting_for_field=" + currentField.getName() + 
                                        " reason=being_stocked");
                        continue;
                    }
                    
                    // Check if there are animals to buy
                    if (currentField.getCurrentCount() > 0) {
                        // Buy an animal
                        if (currentField.removeAnimals(1)) {
                            int waited = waitedTicks;
                            waitedTicks = 0;
                            worldState.removeWaitingBuyer(currentField.getName());
                            
                            // Purchase successful - reset field and timeout
                            String animalType = currentField.getName();
                            worldState.updateBuyerActivity(buyerName, "Bought a " + animalType);
                            
                            // Print appropriate message
                            if (waited > 0) {
                                System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                                " buyer=" + buyerName + " bought 1 animal from " + currentField.getName() + 
                                                " waited_ticks=" + waited);
                            } else {
                                System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                                " buyer=" + buyerName + " bought 1 animal from " + currentField.getName());
                            }
                            
                            // Get a new field next time
                            currentField = null;
                        }
                    } else {
                        // Field is empty, increment wait counter
                        waitedTicks++;
                        String animalType = currentField.getName();
                        
                        // Update status to show animal type
                        worldState.updateBuyerActivity(buyerName, "Waiting for " + animalType + " (" + waitedTicks + " ticks)");
                        worldState.addWaitingBuyer(currentField.getName());
                        
                        // Only log every 10 ticks to reduce spam
                        if (waitedTicks == 1 || waitedTicks % 10 == 0) {
                            System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                            " buyer=" + buyerName + " waiting_for_field=" + currentField.getName() + 
                                            " reason=empty ticks=" + waitedTicks);
                        }
                    }
                }
                
                // Wait a few ticks before trying again
                waitForTicks(random.nextInt(3) + 1);
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