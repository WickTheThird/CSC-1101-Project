// Manages the simulation ticks.
// The TickManager generates ticks at a fixed interval, which can be paused and resumed.
class TickManager extends Thread {
    private final int tickSize; // Time between ticks in milliseconds
    private int currentTick = 0;
    private boolean running = true;
    private boolean paused = false;
    private final WorldState worldState = WorldState.getInstance();

    // Constructor to set the tick size
    public TickManager(int tickSize) {
        this.tickSize = tickSize;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Wait for the specified tick size
                Thread.sleep(tickSize);

                synchronized (this) {
                    // If paused, wait until resumed
                    while (paused && running) {
                        wait();
                    }

                    // If still running after resume, increment the tick
                    if (running) {
                        incrementTick();
                    }
                }
            } catch (InterruptedException e) {
                // Restore interrupt status and exit loop
                Thread.currentThread().interrupt();
                break;
            }
        }
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

    // Check if tick generation is currently paused
    public synchronized boolean isPaused() {
        return paused;
    }
}
