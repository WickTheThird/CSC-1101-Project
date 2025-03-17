import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Field {
    private final String name;
    private static final int capacity = Config.FIELD_CAPACITY;
    private int currentCount;
    private boolean beingStocked = false;
    private final WorldState worldState = WorldState.getInstance();
    
    // ReentrantLock with fairness policy set to true for first in first out ordering
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition stockingCondition = lock.newCondition();
    private final Condition animalAvailableCondition = lock.newCondition();

    public Field(String name) {
        this.name = name;
        this.currentCount = Config.FIELD_INITIAL_ANIMAL_COUNT;
    }
    // Simple getter methods for name, currentCount, and capacity (used primarily by Farmers & Buyers for accurate logging)
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

    // Using a ReentrantLock to ensure thread safety when adding animals to the field
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

    // Uses condition variables to ensure that only one farmer can stock the field at a time
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

    // Used by Farmers and Buyers to check if the field is currently being stocked (to avoid conflicts)
    public boolean isBeingStocked() {
        lock.lock();
        try {
            return beingStocked;
        } finally {
            lock.unlock();
        }
    }
    
    // Uses signalAll() to notify all waiting threads that the field is no longer being stocked
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

    // Method used by Buyers to attempt to buy an animal from the field
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
