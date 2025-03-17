import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class FarmGUI extends JFrame {
    private final WorldState worldState = WorldState.getInstance();
    private final JPanel fieldsPanel;
    private final JPanel farmersPanel;
    private final JPanel buyersPanel;
    private final JPanel enclosurePanel;
    private final JLabel tickLabel;
    private boolean simulationEnded = false;
    private final TickManager tickManager;
    private final JButton pauseButton;
    private final JButton playButton;
    private final Farm farm;
    private int farmerCounter;
    private int buyerCounter;

    public FarmGUI(TickManager tickManager, Farm farm) {
        this.tickManager = tickManager;
        this.farm = farm;
        this.farmerCounter = Config.NUMBER_OF_FARMERS;
        this.buyerCounter = Config.NUMBER_OF_BUYERS;

        setTitle("Farm Simulation");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create panels
        fieldsPanel = new JPanel(new GridLayout(0, 1));
        farmersPanel = new JPanel(new GridLayout(0, 1));
        buyersPanel = new JPanel(new GridLayout(0, 1));
        enclosurePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tickLabel = new JLabel("Tick: 0", JLabel.CENTER);
        tickLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Add titles to sections
        JLabel fieldsTitle = new JLabel("Fields", JLabel.CENTER);
        fieldsTitle.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel farmersTitle = new JLabel("Farmers", JLabel.CENTER);
        farmersTitle.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel buyersTitle = new JLabel("Buyers", JLabel.CENTER);
        buyersTitle.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel enclosureTitle = new JLabel("Enclosure", JLabel.CENTER);
        enclosureTitle.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pauseButton = new JButton("Pause");
        playButton = new JButton("Play");

        JButton addFarmerButton = new JButton("Add Farmer");
        JButton addBuyerButton = new JButton("Add Buyer");
        JButton addDeliveryButton = new JButton("Add Delivery");
        playButton.setEnabled(false);

        pauseButton.addActionListener(e -> {
            if (!simulationEnded && tickManager != null) {
                tickManager.pauseTicks();
                pauseButton.setEnabled(false);
                playButton.setEnabled(true);
            }
        });

        playButton.addActionListener(e -> {
            if (!simulationEnded && tickManager != null) {
                tickManager.resumeTicks();
                playButton.setEnabled(false);
                pauseButton.setEnabled(true);
            }
        });

        addFarmerButton.addActionListener(e -> addNewFarmer());

        addBuyerButton.addActionListener(e -> addNewBuyer());

        addDeliveryButton.addActionListener(e -> addDelivery());

        controlPanel.add(pauseButton);
        controlPanel.add(playButton);
        controlPanel.add(addFarmerButton);
        controlPanel.add(addBuyerButton);
        controlPanel.add(addDeliveryButton);

        // Add panels to the frame - SWAPPED buyers and fields positions
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(buyersTitle, BorderLayout.NORTH);
        westPanel.add(new JScrollPane(buyersPanel), BorderLayout.CENTER);
        westPanel.setPreferredSize(new Dimension(300, 300));

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(farmersTitle, BorderLayout.NORTH);
        eastPanel.add(new JScrollPane(farmersPanel), BorderLayout.CENTER);
        eastPanel.setPreferredSize(new Dimension(300, 300));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(fieldsTitle, BorderLayout.NORTH);
        southPanel.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);
        southPanel.setPreferredSize(new Dimension(800, 150));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(controlPanel, BorderLayout.NORTH);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(tickLabel, BorderLayout.NORTH);
        northPanel.add(enclosureTitle, BorderLayout.CENTER);
        northPanel.add(enclosurePanel, BorderLayout.SOUTH);

        topContainer.add(northPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);
        add(westPanel, BorderLayout.WEST);
        add(eastPanel, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Method to dynamically add a new farmer to the simulation
    private void addNewFarmer() {
        if (!simulationEnded && farm != null) {
            farmerCounter++;
            String farmerName = String.valueOf(farmerCounter);
            Farmer newFarmer = new Farmer(farm, farmerName, tickManager);
            newFarmer.start();
            System.out.println("Added new farmer " + farmerName + " to the simulation");
        }
    }

    private void addNewBuyer() {
        if (!simulationEnded && farm != null) {
            buyerCounter++;
            String buyerName = String.valueOf(buyerCounter);
            Buyer newBuyer = new Buyer(buyerName, farm, tickManager);
            newBuyer.start();
            System.out.println("Added new buyer " + buyerName + " to the simulation");
        }
    }

    private void addDelivery() {
        if (!simulationEnded && farm != null) {
            List<String> animals = DeliveryManager.generateDelivery();

            FarmLogger.logDelivery(FarmLogger.formatDelivery(animals));

            farm.addToEnclosure(animals);

            System.out.println("Manual delivery added to the farm");
        }
    }

    public void update() {
        SwingUtilities.invokeLater(() -> {
            updateFields();
            updateFarmers();
            updateBuyers();
            updateEnclosure();
            if (!simulationEnded) {
                updateTick();
            }
        });
    }

    private void updateFields() {
        fieldsPanel.removeAll();
        for (Map.Entry<String, WorldState.FieldState> entry : worldState.getFieldStates().entrySet()) {
            JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            String status = entry.getValue().isBeingStocked() ? "Stocking" : "Ready";
            JLabel label = new JLabel(entry.getKey() + ": " + entry.getValue().animalCount() + " animals (" + status + ")");

            fieldPanel.setBackground(getStatusColor(status, entry.getValue().animalCount()));

            fieldPanel.add(label);
            fieldsPanel.add(fieldPanel);
        }
        fieldsPanel.revalidate();
        fieldsPanel.repaint();
    }

    private void updateFarmers() {
        farmersPanel.removeAll();
        for (Map.Entry<String, String> entry : worldState.getFarmerActivities().entrySet()) {
            JPanel farmerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label = new JLabel(entry.getKey() + ": " + entry.getValue());

            farmerPanel.setBackground(getStatusColor(entry.getValue()));

            farmerPanel.add(label);
            farmersPanel.add(farmerPanel);
        }
        farmersPanel.revalidate();
        farmersPanel.repaint();
    }

    private void updateBuyers() {
        buyersPanel.removeAll();
        for (Map.Entry<String, String> entry : worldState.getBuyerActivities().entrySet()) {
            JPanel buyerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label = new JLabel(entry.getKey() + ": " + entry.getValue());

            buyerPanel.setBackground(getStatusColor(entry.getValue()));

            buyerPanel.add(label);
            buyersPanel.add(buyerPanel);
        }
        buyersPanel.revalidate();
        buyersPanel.repaint();
    }

    private void updateEnclosure() {
        enclosurePanel.removeAll();
        enclosurePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        enclosurePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        enclosurePanel.setBackground(new Color(240, 240, 250));

        boolean hasAnimals = false;

        for (Map.Entry<String, Integer> entry : worldState.getEnclosureState().entrySet()) {
            if (entry.getValue() > 0) {
                JPanel animalPanel = new JPanel();
                animalPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new Color(180, 180, 220), 1, true),
                    javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                animalPanel.setBackground(new Color(230, 240, 255));

                JLabel animalLabel = new JLabel(entry.getKey() + ": " + entry.getValue());
                animalLabel.setFont(new Font("Arial", Font.BOLD, 14));
                animalPanel.add(animalLabel);
                enclosurePanel.add(animalPanel);
                hasAnimals = true;
            }
        }

        if (!hasAnimals) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(220, 180, 180), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            emptyPanel.setBackground(new Color(255, 230, 230));

            JLabel emptyLabel = new JLabel("Empty");
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 14));
            emptyPanel.add(emptyLabel);
            enclosurePanel.add(emptyPanel);
        }

        enclosurePanel.revalidate();
        enclosurePanel.repaint();
    }
    
    private void updateTick() {
        tickLabel.setText("Tick: " + worldState.getCurrentTick());
    }

    // Helper method for color coding based on status/activity - might be worth moving to a utility class
    private Color getStatusColor(String activity) {
        if (activity.contains("On break")) {
            return new Color(255, 182, 193); // Light pink
        } else if (activity.contains("Walking")) {
            return new Color(200, 220, 255); // Light blue
        } else if (activity.contains("Stocking")) {
            return new Color(255, 220, 200); // Light orange
        } else if (activity.contains("Waiting") || activity.contains("waiting")) {
            return new Color(230, 230, 230); // Light gray
        } else if (activity.contains("Moving")) {
            return new Color(200, 235, 255); // Light cyan
        } else if (activity.contains("Returning")) {
            return new Color(220, 220, 255); // Light lavender
        } else if (activity.contains("Bought") || activity.contains("bought")) {
            return new Color(220, 255, 220); // Light green
        } else {
            return new Color(255, 255, 200); // Default light yellow
        }
    }

    // Overloaded method specifically for field status since it uses different conditions
    private Color getStatusColor(String status, int animalCount) {
        if (status.equals("Stocking")) {
            return new Color(255, 220, 200); // Light orange when being stocked
        } else if (animalCount > 0) {
            return new Color(220, 255, 220); // Light green when has animals
        } else {
            return new Color(255, 200, 200); // Light red when empty
        }
    }

    public void showSimulationEnded() {
        SwingUtilities.invokeLater(() -> {
            simulationEnded = true;
            tickLabel.setText("SIMULATION ENDED");
            tickLabel.setFont(new Font("Arial", Font.BOLD, 36));
            tickLabel.setForeground(Color.RED);
            pauseButton.setEnabled(false);
            playButton.setEnabled(false);
        });
    }
}
