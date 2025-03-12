class TickManager extends Thread {
    private final int tickSize;
    private int currentTick = 0;
    private boolean running = true;
    private boolean paused = false;
    private final WorldState worldState = WorldState.getInstance();

    public TickManager(int tickSize) {
        this.tickSize = tickSize;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(tickSize);
                
                synchronized (this) {
                    while (paused && running) {
                        wait();
                    }
                    
                    if (running) {
                        incrementTick();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public synchronized void incrementTick() {
        currentTick++;
        worldState.updateTick(currentTick);
        notifyAll();
    }

    public synchronized int getCurrentTick() {
        return currentTick;
    }

    public synchronized void stopTicks() {
        running = false;
        notifyAll();
    }
    
    public synchronized void pauseTicks() {
        paused = true;
        FarmLogger.logPaused(currentTick);
    }
    
    public synchronized void resumeTicks() {
        paused = false;
        notifyAll();
        FarmLogger.logResumed(currentTick);
    }
    
    public synchronized boolean isPaused() {
        return paused;
    }
}
