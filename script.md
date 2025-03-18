# 5-Minute Farm Simulation Project Presentation Script

## (0:00 - 0:20) Introduction

**Christopher:**
Hello everyone, I'm Christopher, and together with Filip, we developed a multi-threaded farm simulation for the CSC1101 concurrency project. Today, we'll briefly explain our design choices and demonstrate how our solution manages concurrency effectively.

## (0:20 - 0:45) Project Overview

**Filip:**
Our farm simulation involves Farmers, Buyers, a DeliveryManager, and a TickManager. Each runs as an independent thread:

- Farmers move animals from the enclosure to the correct fields
- Buyers purchase animals from fields at random intervals
- DeliveryManager introduces new animals into the enclosure based on probability
- TickManager advances the simulation "clock"

Our main challenge was ensuring thread-safe access to shared resources while avoiding deadlocks and starvation.

## (0:45 - 1:15) Concurrency Strategies

**Christopher:**
We implemented several concurrency patterns:

- The monitor pattern with synchronized methods in Farm.java controls access to the enclosure
- ReentrantLock with fairness enabled in Field.java ensures FIFO ordering when multiple threads compete
- Condition variables let threads wait for specific state changes
- A central TickManager synchronizes all activities in the simulation

Let me show you some key code sections that handle this synchronization...

As you can see here, we are using reentrant lock with the parameter fair, which enforces locking of threads and releases access in a first in first out order, so that those waiting the longest to access a field in this case, are given firts priority.

We have also set two conditiuons, stockingCondition, and animalAvailableCondition. These can be used to signal to other threads that they will need to wait for the thread to be unlocked before being able to access it as you can see here, and then once a condition has been met, they can signal to other threads that the resource is available and in turn unlock it.

_[Shows Field.java's ReentrantLock implementation and Condition variables]_

## (1:15 - 2:00) Code Walkthrough & Demonstration

**Filip:**
Let's compile and run the simulation using our Makefile:

```bash
make all
```

This compiles all Java files and starts the simulation with the GUI. As you can see, the GUI displays:

- The enclosure at the top showing current animals waiting
- Fields at the bottom with their current counts
- Farmers on the right and Buyers on the left showing their activities

Notice how the fields are color-coded - green when they have animals, red when empty, and orange when being stocked.

**Christopher:**
We can also interact with the running simulation. Let me add another farmer and buyer to show dynamic scaling:

_[Clicks "Add Farmer" and "Add Buyer" buttons]_

You can see they immediately join the simulation. We can also manually add a delivery:

_[Clicks "Add Delivery" button]_

Look at the enclosure - it just received 10 new random animals.

## (2:00 - 3:20) Simulation Logs & Concurrency Demonstration

**Filip:**
In the console, we can see detailed logging of all events:

_[Points to console output]_

```
15 21 delivery_arrived : cows=3 pigs=4 sheep=3
16 22 farmer=1 took 10 animals from the enclosure.
16 22 farmer=1 moving_to_field=pigs time=14 animals=4
30 22 farmer=1 began_stocking_field : pigs=4
34 22 farmer=1 finished_stocking_field : pigs=4
35 22 farmer=1 moving_to_field=cows time=13 animals=3
```

Each line includes the tick count and thread ID, making it easy to track concurrent operations. Notice how buyer threads wait when a field is being stocked:

```
48 25 buyer=2 waiting_for_field=cows reason=being_stocked
48 26 buyer=3 waiting_for_field=pigs reason=empty
```

## (3:20 - 4:15) Fairness & Starvation Prevention

**Christopher:**
Our solution addresses fairness and starvation through several mechanisms:

1. Fair locking with `ReentrantLock(true)` ensures first-come-first-served access
2. Buyers also have a timeout, as you can see here, which helps to prevent starvation as once a specified number of ticks have passed, the buyer will give up and move on, preventing indefinite blocking

3. In terms of field prioritization - Farmers prioritize fields with waiting buyers, as you can see here which helpps to reduce the time that buyers wait and hopefully provides a more strategic stocking pattern to prevent less idle waiting accross our application:

   _[Shows in Farmer.java the getSortedAnimals method]_

   ```java
   if (aHasWaiting && !bHasWaiting) return -1;
   if (!aHasWaiting && bHasWaiting) return 1;
   ```

## (4:15 - 5:00) Configurability & Wrap-Up

**Filip:**
All simulation parameters are easily configurable through Config.java:

_[Opens Config.java]_

```java
public static final int TICK_SIZE = 100;
public static final int NUMBER_OF_FARMERS = 3;
public static final int DELIVERY_FREQUENCY = 100;
public static final int FIELD_CAPACITY = 50;
```

By adjusting these values, we can simulate different scenarios - more farmers, faster deliveries, or limited field capacity to create more contention.

**Christopher:**
In conclusion, our farm simulation demonstrates key concurrency concepts:

- Thread synchronization through locks and conditions
- Resource management with fairness policies
- Deadlock avoidance through careful lock ordering
- Starvation prevention with timeouts and prioritization

Thank you for watching our demonstration!
