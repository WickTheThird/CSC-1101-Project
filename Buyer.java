import java.util.List;
import java.util.Random;

class Buyer extends Thread {
    private final Farm farm;
    private final String buyerName;
    private final Random random = new Random();
    private final TickManager tickManager;
    private int lastCheckedTick = 0;

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
                
                if (random.nextInt(10) == 0) {
                    Field field = getRandomField();
                    if (field != null) {
                        synchronized (field) {
                            if (field.getCurrentCount() > 0) {
                                field.removeAnimals(1);
                                System.out.println(tickManager.getCurrentTick() + " " + buyerName + 
                                                   " bought 1 animal from " + field.getName());
                            } else {
                                System.out.println(tickManager.getCurrentTick() + " " + buyerName + 
                                                   " is waiting for animals in " + field.getName());
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
