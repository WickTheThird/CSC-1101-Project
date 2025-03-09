import java.util.List;
import java.util.Random;

class Buyer extends Thread {
    private final Farm farm;
    private final String buyerName;
    private final Random random = new Random();
    private final TickManager tickManager;
    private int lastCheckedTick = 0;
    private final WorldState worldState = WorldState.getInstance();
    
    // For tracking the waiting state and time
    private Field waitingForField = null;
    private long waitStartTick = 0;

    public Buyer(Farm farm, String buyerName, TickManager tickManager) {
        this.farm = farm;
        this.buyerName = buyerName;
        this.tickManager = tickManager;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                synchronized (tickManager) {
                    int currentTick = tickManager.getCurrentTick();
                    if (lastCheckedTick == currentTick) {
                        tickManager.wait();
                    }
                    lastCheckedTick = currentTick;
                }
                
                if (waitingForField != null) {
                    // If we're already waiting for a field, check if it has animals now
                    synchronized (waitingForField) {
                        if (waitingForField.getCurrentCount() > 0) {
                            // Animal is available, buy it and stop waiting
                            waitingForField.removeAnimals(1);
                            long waitedTicks = tickManager.getCurrentTick() - waitStartTick;
                            
                            String message = String.format("%d %s bought 1 animal from %s waited_ticks=%d",
                                tickManager.getCurrentTick(), buyerName, 
                                waitingForField.getName(), waitedTicks);
                            
                            worldState.updateBuyerActivity(buyerName, message);
                            System.out.println(message);
                            
                            // Update field state in WorldState
                            worldState.updateFieldState(waitingForField.getName(), 
                                                        waitingForField.getCurrentCount(), false);
                            waitingForField = null; // No longer waiting
                        }
                    }
                } else if (random.nextInt(10) == 0) {
                    // Not waiting and random chance to attempt purchase
                    Field field = getRandomField();
                    if (field != null) {
                        synchronized (field) {
                            if (field.getCurrentCount() > 0) {
                                field.removeAnimals(1);
                                
                                String message = String.format("%d %s bought 1 animal from %s",
                                    tickManager.getCurrentTick(), buyerName, field.getName());
                                
                                worldState.updateBuyerActivity(buyerName, message);
                                System.out.println(message);
                                
                                // Update field state in WorldState
                                worldState.updateFieldState(field.getName(), field.getCurrentCount(), false);
                            } else {
                                // Start waiting for this field to have animals
                                waitingForField = field;
                                waitStartTick = tickManager.getCurrentTick();
                                
                                String message = String.format("%d %s is waiting for animals in %s",
                                    tickManager.getCurrentTick(), buyerName, field.getName());
                                
                                worldState.updateBuyerActivity(buyerName, message);
                                System.out.println(message);
                            }
                        }
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Field getRandomField() {
        List<Field> fields = farm.getFields();
        if (fields.isEmpty()) return null;
        return fields.get(random.nextInt(fields.size()));
    }
}