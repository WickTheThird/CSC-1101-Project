import java.util.List;

class Buyer extends Thread{

    public final Farm farm;
    private final String buyerName;

    public Buyer(Farm farm, String buyerName) {
        this.farm = farm;
        this.buyerName = buyerName;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);

                Field field = getRandomField();
                if (field != null) {
                    synchronized (field) {
                        if (field.getCurrentCount() > 0) {
                            field.removeAnimals(1);
                            System.out.println(buyerName + " bought 1 animal from " + field.getName());
                        } else {
                            System.out.println(buyerName + " is waiting for animals in " + field.getName());
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Field getRandomField() {
        List<Field> fields = farm.getFields();

        if (fields.isEmpty()) {
            return null;
        }

        int randomIndex = (int) (Math.random() * fields.size());
        return fields.get(randomIndex);
    }

}
