class TickManager extends Thread {
    private final int tickSize;
    private int currentTick = 0;
    private boolean running = true;
    private final WorldState worldState = WorldState.getInstance();

    public TickManager(int tickSize) {
        this.tickSize = tickSize;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(tickSize);
                incrementTick();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public synchronized void incrementTick() {
        currentTick++;
        System.out.println("Tick: " + currentTick);
        worldState.updateTick(currentTick);
        notifyAll();
    }

    public synchronized int getCurrentTick() {
        return currentTick;
    }

    public synchronized void stopTicks() {
        running = false;
    }
}
