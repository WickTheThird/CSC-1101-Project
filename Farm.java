import java.util.ArrayList;
import java.util.List;

public class Farm {
    private List<String> enclosure = new ArrayList<>();
    private List<Field> fields = new ArrayList<>();
    private final WorldState worldState = WorldState.getInstance();

    public void addField() {
        fields.add(new Field("pigs"));
        fields.add(new Field("cows"));
        fields.add(new Field("sheep"));
        fields.add(new Field("llamas"));
        fields.add(new Field("chickens"));
        
        // Initialize field states in WorldState
        for (Field field : fields) {
            worldState.initializeField(field.getName(), field.getCurrentCount());
        }
    }

    public synchronized void addToEnclosure(List<String> animals) {
        if (animals == null || animals.isEmpty()) return;
        
        // Add all animals to the enclosure
        enclosure.addAll(animals);
        
        // Update WorldState for GUI
        worldState.addAnimalsToEnclosure(animals);
        worldState.updateEnclosureCount(enclosure.size());
        
        // Notify waiting farmers
        notifyAll();
    }

    public synchronized List<String> takeFromEnclosure(int maxAnimals) {
        while (enclosure.isEmpty()) {
            try {
                wait();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }

        List<String> taken = new ArrayList<>();
        for (int i = 0; i < maxAnimals && !enclosure.isEmpty(); i++) {
            taken.add(enclosure.removeFirst());
        }
        worldState.removeAnimalsFromEnclosure(taken);
        return taken;
    }

    public List<Field> getFields() {
        return fields;
    }

    public synchronized boolean hasAnimalsInEnclosure() {
        return !enclosure.isEmpty();
    }

}
