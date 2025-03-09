import java.util.ArrayList;
import java.util.List;

class Farm {
    private List<String> enclosure = new ArrayList<>();
    private List<Field> fields = new ArrayList<>();


    public void addField() {
        fields.add(new Field("pigField"));
        fields.add(new Field("cowField"));
        fields.add(new Field("sheepField"));
        fields.add(new Field("llamaField"));
        fields.add(new Field("chickenField"));
    }

    public synchronized void addToEnclosure(List<String> animals) {
        enclosure.addAll(animals);
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
            taken.add(enclosure.remove(0));
        }
        return taken;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public int getEnclosureSize() {
        return enclosure.size();
    }

    public void setEnclosureSize(int enclosureSize) {
        this.enclosure = new ArrayList<>(enclosureSize);
    }

}
