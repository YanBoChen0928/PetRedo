package model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/*
    One of the core model, dealing with all the logic about time management.
    !!!
    Using ScheduledExecutorService is a recommended approach for handling periodic tasks:
    1. It provides precise time scheduling.
    2. It supports a scalable thread pool to balance performance.
    3. It accommodates both immediate and delayed execution, making it suitable for
       managing periodic or one-time tasks.
 */

/**
 * Manages time-based events and state updates for the pet.
 * Handles automatic health changes and state score increases.
 */
public class TimeManager {
  // One game day equals 1 minute real time
  private static final long DAY_DURATION = 60_000;
  private final Pet pet;
  private ScheduledExecutorService scheduler;
  private long sleepStartTime;
  private Runnable updateListener;
  private Consumer<String> messageListener;

  /**
   * Creates a new TimeManager for the specified pet.
   * Initializes schedulers for health and state updates.
   *
   * @param pet The pet to manage
   */
  public TimeManager(Pet pet) {
    this.pet = pet;
    startScheduler();
  }

  /**
   * Starts the scheduler for time management.
   */
  private void startScheduler() {
    this.scheduler = Executors.newScheduledThreadPool(1);
    initializeTimers();
  }

  /**
   * Restarts the time management system. (when the game is restarted, that is, the pet is dead)
   */
  public void restart() {
    // reset the pet's status, health, and awake status
    pet.setHealth(Pet.MAX_HEALTH);
    pet.setSleeping(false);

    // should shunt down the current scheduler first
    shutdown();
    startScheduler();  // re-start
  }

  /**
   * Shuts down the scheduler and stops all time-based events.
   */
  public void shutdown() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdownNow();  // If it activates shutdown, then Is shunt down immediately
      try {
        // Wait for the scheduler to terminate
        scheduler.awaitTermination(100, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Sets the update listener for the time manager.
   * The listener is notified when the pet's status changes.
   *
   * @param listener The listener to set
   */

  public void setUpdateListener(Runnable listener) {
    this.updateListener = listener;
  }

  /**
   * Sets the message listener for the time manager.
   * The listener is notified when the pet's status changes.
   *
   * @param listener The listener to set
   */
  public void setMessageListener(Consumer<String> listener) {
    this.messageListener = listener;
  }

  /**
   * Notifies the message listener of a state change.
   * Blocks non-death messages if the pet is dead.
   *
   * @param message The message to send
   */
  public void notifyStateChange(String message) {
    if (messageListener != null) {
      // if the pet is dead, block all non-death messages, the code will not process down
      // to the messageListener.accept(message) line
      if (pet.getHealth() <= 0 && !message.contains("has died")) {
        return;
      }
      messageListener.accept(message);
    }
  }

  private void notifyUpdate() {
    if (updateListener != null) {
      updateListener.run();
    }
  }

  /**
   * Sets up periodic tasks for updating pet status.
   * - Health updates every second
   * - State updates at different intervals based on state type
   */
  private void initializeTimers() {
    // Health update every second
    scheduler.scheduleAtFixedRate(() -> {
      updateHealth();
      notifyUpdate();
    }, 1, 1, TimeUnit.SECONDS);

    // State updates at different intervals
    scheduler.scheduleAtFixedRate(() -> {
      updateState(PetState.HUNGRY);
      notifyUpdate();
    }, 5, 5, TimeUnit.SECONDS);

    scheduler.scheduleAtFixedRate(() -> {
      updateState(PetState.DIRTY);
      notifyUpdate();
    }, 15, 15, TimeUnit.SECONDS);

    scheduler.scheduleAtFixedRate(() -> {
      updateState(PetState.TIRED);
      notifyUpdate();
    }, 15, 15, TimeUnit.SECONDS);

    scheduler.scheduleAtFixedRate(() -> {
      updateState(PetState.BORED);
      notifyUpdate();
    }, 10, 10, TimeUnit.SECONDS);

    // check the sleep time every second
    scheduler.scheduleAtFixedRate(this::checkSleepTime, 1, 1, TimeUnit.SECONDS);
  }

  /**
   * Updates pet's health based on current state.
   * - Decreases health if in critical state
   * - Increases health if normal and below maximum
   * - Increases health while sleeping if below maximum
   */
  private void updateHealth() {
    if (pet.getHealth() <= 0) {
      return;  // if the pet is dead, no need to update health
    }

    if (pet.isSleeping()) {
      if (pet.getHealth() < Pet.MAX_HEALTH) {
        pet.setHealth(Math.min(pet.getHealth() + Pet.HEALTH_RECOVERY_RATE, Pet.MAX_HEALTH));
      }
      return;
    }

    if (pet.getCurrentState() != PetState.NORMAL) {
      int newHealth = pet.getHealth() - Pet.HEALTH_DECREASE_RATE;
      if (newHealth <= 0) {
        shutdown();  // close and stop all schedulers
        // Reset all states to the normal state
        for (PetState state : PetState.values()) {
          if (state != PetState.NORMAL) {
            pet.resetState(state);
          }
        }
        pet.setHealth(0);
        notifyStateChange("Your pet has died!"
            + " Taking care of pets is like nurturing children. "
            + "It never be easy, but it's always worth it.");
        return;
      }
      pet.setHealth(newHealth);
    } else if (pet.getHealth() < Pet.MAX_HEALTH) {
      pet.setHealth(Math.min(pet.getHealth() + Pet.HEALTH_RECOVERY_RATE, Pet.MAX_HEALTH));
    }
  }

  /**
   * Updates a specific state's score.
   * (should be better when changed to name after: updateStateScore)
   * Skips update if pet is sleeping (except for TIRED state).
   *
   * @param state The state to update
   */
  private void updateState(PetState state) {
    if (pet.getHealth() <= 0) {
      return;  // if the pet is dead, no need to update state
    }

    if (pet.isSleeping()) {
      return;  // if the pet is sleeping, no need to update state
    }

    int currentScore = pet.getStateScore(state);
    int increment = state.getWeight();
    int newScore = Math.min(currentScore + increment, Pet.MAX_SCORE);

    if (newScore != currentScore) {
      pet.updateState(state, newScore);
    }
  }

  /**
   * Checks if the pet has been sleeping for a minute and wakes it up if so.
   */

  private void checkSleepTime() {
    if (pet.isSleeping()) {
      long currentTime = System.currentTimeMillis();
      if (currentTime - sleepStartTime >= 60000) {
        pet.setSleeping(false);
        pet.wakeUp();
      }
    }
  }

  /**
   * Starts the sleep timer for the pet.
   */

  public void startSleeping() {
    sleepStartTime = System.currentTimeMillis();
  }
} 