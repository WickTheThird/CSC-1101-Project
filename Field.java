import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Field {
    private final String name;
    private int capacity = 50;
    private int currentCount = 0;
    private boolean beingStocked = false;
    private final WorldState worldState = WorldState.getInstance();
    
    // ReentrantLock with fairness policy set to true for first in firsy out ordering
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition stockingCondition = lock.newCondition();
    private final Condition animalAvailableCondition = lock.newCondition();

    public Field(String name) {
        this.name = name;
        this.currentCount = 5;
    }

    public String getName() {
        lock.lock();
        try {
            return name;
        } finally {
            lock.unlock();
        }
    }

    public int getCurrentCount() {
        lock.lock();
        try {
            return currentCount;
        } finally {
            lock.unlock();
        }
    }

    public int getCapacity() {
        lock.lock();
        try {
            return capacity;
        } finally {
            lock.unlock();
        }
    }

    public boolean canAddAnimals(int count) {
        lock.lock();
        try {
            return currentCount + count <= capacity;
        } finally {
            lock.unlock();
        }
    }

    public void addAnimals(int count) {
        lock.lock();
        try {
            currentCount += count;
            worldState.updateFieldCount(name, currentCount);
            // Signal all waiting buyers that animals are now available
            animalAvailableCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean removeAnimals(int count) {
        lock.lock();
        try {
            if (currentCount >= count) {
                currentCount -= count;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public void startStocking() throws InterruptedException {
        lock.lock();
        try {
            while (beingStocked) {
                stockingCondition.await();
            }
            beingStocked = true;
            worldState.updateFieldState(name, currentCount, true);
        } finally {
            lock.unlock();
        }
    }

    public boolean isBeingStocked() {
        lock.lock();
        try {
            return beingStocked;
        } finally {
            lock.unlock();
        }
    }
    
    public void finishStocking() {
        lock.lock();
        try {
            beingStocked = false;
            worldState.updateFieldState(name, currentCount, false);
            stockingCondition.signalAll();
            animalAvailableCondition.signalAll(); // Signal waiting buyers that animals might be available now
        } finally {
            lock.unlock();
        }
    }

    public boolean tryRemoveAnimal() {
        lock.lock();
        try {
            if (beingStocked) {
                return false;
            }
            
            if (currentCount > 0) {
                currentCount--;
                worldState.updateFieldCount(name, currentCount);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}
