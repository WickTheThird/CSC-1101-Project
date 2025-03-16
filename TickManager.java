// Manages the simulation ticks.
// The TickManager generates ticks at a fixed interval, which can be paused and resumed.
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickManager extends Thread {
    private final int tickSize;
    private int currentTick = 0;
    private boolean running = true;
    private boolean paused = false;
    private final WorldState worldState = WorldState.getInstance();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Constructor to set the tick size
    public TickManager(int tickSize) {
        this.tickSize = tickSize;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (TickManager.this) {
                if (!paused && running) {
                    incrementTick();
                }
            }
        }, tickSize, tickSize, TimeUnit.MILLISECONDS);
    }

    // Increment the current tick count and notify all waiting threads
    public synchronized void incrementTick() {
        currentTick++;
        worldState.updateTick(currentTick);
        notifyAll();
    }

    // Get the current tick count
    public synchronized int getCurrentTick() {
        return currentTick;
    }

    // Stop the tick generation and notify all waiting threads
    public synchronized void stopTicks() {
        running = false;
        notifyAll();
        scheduler.shutdownNow();
    }

    // Pause tick generation
    public synchronized void pauseTicks() {
        paused = true;
        FarmLogger.logPaused(currentTick);
    }

    // Resume tick generation
    public synchronized void resumeTicks() {
        paused = false;
        notifyAll();
        FarmLogger.logResumed(currentTick);
    }

}
