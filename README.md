# Farm Simulation Project

## Overview
This multi-threaded Java application simulates a farm with animals, farmers, and buyers. Animals arrive in batches, farmers move them to appropriate fields, and buyers purchase them from fields. The simulation measures time in "ticks" and demonstrates concurrent programming concepts including synchronization, thread safety, and resource management.

## Requirements
- Java SE 21 (LTS) or higher
- No external libraries required

## Compiling and Running

### Using Makefile
```bash
# Build and run with GUI
make all

# Build only (creates .class files)
make compile

# Run with GUI after compiling
make run-gui  

# Run in headless mode after compiling
make run

# Clean up compiled files
make clean
```

### Using Java directly
```bash
# Compile all source files
javac *.java

# Run with GUI
java Main --gui

# Run without GUI (headless mode)
java Main
```

## Configuration
All simulation parameters can be adjusted in Config.java:

```java
public class Config {
    public static final int TICK_SIZE = 100;           // Milliseconds per tick
    public static final int NUMBER_OF_FARMERS = 3;     // Initial farmer count
    public static final int NUMBER_OF_BUYERS = 3;      // Initial buyer count
    public static final int DELIVERY_FREQUENCY = 100;  // Average ticks between deliveries
    public static final int SIMULATION_DURATION = 10000; // Total ticks before ending
    public static final int DELIVERY_SIZE = 10;        // Animals per delivery
    public static final int FARMER_BREAK_CHANCE = 5;   // % chance of break each tick
    public static final int FARMER_BREAK_MIN_DURATION = 20; // Min break duration
    public static final int FARMER_BREAK_MAX_DURATION = 40; // Max break duration
    public static final int FIELD_CAPACITY = 50;       // Maximum animals per field
    public static final int FIELD_INITIAL_ANIMAL_COUNT = 5; // Starting animals per field
    public static final int FARMER_MAX_ANIMALS = 10;   // Max animals a farmer can carry
}
```

## GUI Features
When running with `--gui`:
- Visualize fields, farmers, buyers and the enclosure
- Pause/resume the simulation
- Add new farmers, buyers, or deliveries dynamically
- Color-coded status indicators

## Output
- Terminal output shows key events with tick counts and thread IDs
- A log file (`farm_simulation_log.txt`) records detailed simulation state

## Notes
- The simulation will automatically stop after reaching `SIMULATION_DURATION` ticks
- The farm contains five fields: pigs, cows, sheep, llamas, and chickens
- Farmers prioritize fields with waiting buyers to minimize wait times
