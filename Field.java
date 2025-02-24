
class Field {
    private final String name;
    private int capacity = 50;
    private int currentCount = 0;

    public Field(String name) {
        this.name = name;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized int getCurrentCount() {
        return currentCount;
    }

    public synchronized int getCapacity() {
        return capacity;
    }

    public boolean canAddAnimals(int count) {
        return currentCount + count <= capacity;
    }

    public synchronized void addAnimals(int count) {
        currentCount += count;
    }

    public synchronized boolean removeAnimals(int count) {
        if (currentCount >= count) {
            currentCount -= count;
            return true;
        }
        return false;
    }
}
