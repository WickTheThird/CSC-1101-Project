public class FarmLogger {
    private static TickManager tickManager;
    private static int lastLoggedTick = -1;
    
    public static void setTickManager(TickManager manager) {
        tickManager = manager;
    }
    
    public static synchronized void logDelivery(String formattedAnimals) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " delivery_arrived : " + formattedAnimals);
    }
    
    public static synchronized void logBuyerCollection(String buyerName, String fieldName, int waitedTicks) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        
        if (waitedTicks > 0) {
            System.out.println(tickCount + " " + threadId + " buyer=" + buyerName + 
                    " bought 1 animal from " + fieldName + " waited_ticks=" + waitedTicks);
        } else {
            System.out.println(tickCount + " " + threadId + " buyer=" + buyerName + 
                    " bought 1 animal from " + fieldName);
        }
    }
    
    public static synchronized void logBuyerWaiting(String buyerName, String fieldName, String reason) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " buyer=" + buyerName + 
                " waiting_for_field=" + fieldName + " reason=" + reason);
    }
    
    public static synchronized void logBuyerGaveUp(String buyerName, String fieldName, int waitedTicks) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " buyer=" + buyerName + 
                " gave_up_waiting for " + fieldName + " after " + waitedTicks + " ticks");
    }
    
    public static synchronized void logFarmerCollection(String farmerName, int animalCount) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " farmer=" + farmerName + 
                " took " + animalCount + " animals from the enclosure.");
    }
    
    public static synchronized void logFarmerBeginStocking(String farmerName, String fieldName, int count) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " farmer=" + farmerName + 
                " began_stocking_field : " + fieldName + "=" + count);
    }
    
    public static synchronized void logFarmerFinishStocking(String farmerName, String fieldName, int count) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " farmer=" + farmerName + 
                " finished_stocking_field : " + fieldName + "=" + count);
    }
    
    public static synchronized void logFarmerMoving(String farmerName, String fieldName, int movementTime, int animalCount) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " farmer=" + farmerName + 
                " moving_to_field=" + fieldName + " time=" + movementTime + " animals=" + animalCount);
    }
    
    public static synchronized void logFarmerReturning(String farmerName, int returnTime) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " farmer=" + farmerName + 
                " returning_to_enclosure time=" + returnTime);
    }
    
    public static synchronized void logFarmerBreak(String farmerName, int duration) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " farmer=" + farmerName + 
                " taking_break duration=" + duration);
    }
    
    public static synchronized void logFarmerBreakEnded(String farmerName) {
        int tickCount = tickManager.getCurrentTick();
        checkAndLogTickHeader(tickCount);
        
        long threadId = Thread.currentThread().threadId();
        System.out.println(tickCount + " " + threadId + " farmer=" + farmerName + " break_ended");
    }
    
    private static void checkAndLogTickHeader(int currentTick) {
        if (currentTick != lastLoggedTick) {
            lastLoggedTick = currentTick;
        }
    }
    
    public static synchronized void logPaused(int tickCount) {
        checkAndLogTickHeader(tickCount);
        System.out.println("Simulation paused at tick: " + tickCount);
    }
    
    public static synchronized void logResumed(int tickCount) {
        checkAndLogTickHeader(tickCount);
        System.out.println("Simulation resumed at tick: " + tickCount);
    }
}
