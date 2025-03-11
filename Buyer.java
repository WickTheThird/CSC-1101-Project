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
                
                // Get a random field
                Field field = getRandomField();
                if (field == null) continue;
                
                // Try to buy from field
                synchronized (field) {
                    // Check if field is being stocked
                    if (field.isBeingStocked()) {
                        waitedTicks++;
                        worldState.updateBuyerActivity(buyerName, "Waiting - " + field.getName() + " is being stocked");
                        worldState.addWaitingBuyer(field.getName());
                        
                        System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                        " buyer=" + buyerName + " waiting_for_field=" + field.getName() + 
                                        " reason=being_stocked");
                        continue;
                    }
                    
                    // Check if there are animals to buy
                    if (field.getCurrentCount() > 0) {
                        // Buy an animal
                        if (field.removeAnimals(1)) {
                            int waited = waitedTicks;
                            waitedTicks = 0;
                            worldState.removeWaitingBuyer(field.getName());
                            
                            // Update with animal type instead of field number
                            String animalType = field.getName();
                            worldState.updateBuyerActivity(buyerName, "Bought a " + animalType + " animal");
                            
                            if (waited > 0) {
                                System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                                " buyer=" + buyerName + " bought 1 animal from " + field.getName() + 
                                                " waited_ticks=" + waited);
                            } else {
                                System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                                " buyer=" + buyerName + " bought 1 animal from " + field.getName());
                            }
                        }
                    } else {
                        // Field is empty, mark as waiting
                        waitedTicks++;
                        String animalType = field.getName();
                        
                        // Update status to show animal type
                        worldState.updateBuyerActivity(buyerName, "Waiting for " + animalType);
                        worldState.addWaitingBuyer(field.getName());
                        
                        System.out.println(tickManager.getCurrentTick() + " " + Thread.currentThread().threadId() + 
                                        " buyer=" + buyerName + " waiting_for_field=" + field.getName() + 
                                        " reason=empty");
                    }
                }
                
                // Wait a few ticks before trying again
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