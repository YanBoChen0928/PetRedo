package model;

import java.util.concurrent.ConcurrentHashMap;
import model.state.BoredState;
import model.state.DirtyState;
import model.state.HappyState;
import model.state.HungryState;
import model.state.NormalState;
import model.state.PetStateBase;
import model.state.SleepingState;
import model.state.TiredState;

/**
 * Represents a virtual pet with various states and attributes.
 * The pet has health, different states (hungry, tired, etc.), and can perform various actions.
 */
public class Pet {
  // Constants for pet attributes
  public static final int MAX_HEALTH = 100;
  public static final int MAX_SCORE = 10;
  public static final int HEALTH_DECREASE_RATE = 2;
  public static final int HEALTH_RECOVERY_RATE = 5;
  // Core attributes
  private int health;
  // it's not final here, it will update and change the state scores
  private ConcurrentHashMap<PetState, Integer> stateScores;
  private PetState currentState;
  private boolean isSleeping;
  private long lastActionTime;
  private PetStateBase currentStateObject;
  private TimeManager timeManager;  // add TimeManager
  private PetState previousState = PetState.NORMAL;  // give previous state a normal state

  /**
   * Creates a new pet with default values.
   * Initial health is set to maximum, state is normal, and all state scores are 0.
   */
  public Pet() {
    this.health = MAX_HEALTH;
    // use concurrent
    this.stateScores = new ConcurrentHashMap<>();
    this.currentState = PetState.NORMAL;
    this.isSleeping = false;
    initializeStates();
    this.currentStateObject = new NormalState(this);
  }

  /**
   * Get the TimeManager associated with the pet for some time-based operations.
   * Like notifying the user when the pet needs attention.
   * Like when the pet is going to wake up.
   *
   * @return The TimeManager associated with the pet
   */
  public TimeManager getTimeManager() {
    return timeManager;
  }

  /**
   * Set the TimeManager associated with the pet for some time-based operations.
   * Like notifying the user when the pet needs attention.
   * Like when the pet is going to wake up.
   *
   * @param timeManager The TimeManager to associate with the pet
   */
  public void setTimeManager(TimeManager timeManager) {
    this.timeManager = timeManager;
  }

  /**
   * Initializes all possible states with score 0.
   * Sets the initial timestamp for action tracking.
   */
  protected void initializeStates() {
    for (PetState state : PetState.values()) {
      if (state != PetState.NORMAL) {
        stateScores.put(state, 0);
      }
    }
    this.lastActionTime = System.currentTimeMillis();
  }

  /**
   * Updates the score for a specific state and recalculates current state.
   *
   * @param state The state to update
   * @param score The new score value (will be capped at MAX_SCORE)
   */
  public void updateState(PetState state, int score) {
    if (health <= 0) {
      // !important: to determine the logic of what's going to do when the pet is dead
      return;
    }

    if (state != PetState.NORMAL) {
      //restrict the score to be within 0 and MAX_SCORE
      int currentScore = Math.min(Math.max(score, 0), MAX_SCORE);
      stateScores.put(state, currentScore);
      if (currentScore >= MAX_SCORE && timeManager != null) {  // 檢查 timeManager 是否存在
        PetAction action = PetAction.getActionForState(state);
        timeManager.notifyStateChange(String.format("Your pet needs %s! Please %s your pet!",
            action.toString().toLowerCase(),
            action.toString().toLowerCase()));
      }
      updateCurrentState();
    }
  }

  /**
   * Updates the current state based on state scores and weights.
   */
  public void updateCurrentState() {
    if (isSleeping) {
      currentState = PetState.TIRED;
      // if sleeping, set state to TIRED because sleeping state is not in the enum
      currentStateObject = new SleepingState(this);
      return;
    }

    PetState criticalState = null;
    int maxWeight = 0;

    // the priority of the state is decided by the weight
    // if there are more than one state with the same score,
    // the state with the higher weight will be chosen to display and notify
    // the user to take care of the pet
    for (PetState state : stateScores.keySet()) {
      // .keySet() returns a Set view of the keys contained in the map
      // (here is concurrentHashMap), and the key is the state.
      if (stateScores.get(state) >= MAX_SCORE) {
        // Map.get(Object key) returns the value to which the specified key is mapped,
        if (state.getWeight() > maxWeight) {
          maxWeight = state.getWeight();
          criticalState = state;
        }
      }
    }

    PetState newState = (criticalState != null) ? criticalState : PetState.NORMAL;
    this.currentState = newState;

    // create a new state object based on the current state
    switch (currentState) {
      case NORMAL:
        currentStateObject = new NormalState(this);
        break;
      case HUNGRY:
        currentStateObject = new HungryState(this);
        break;
      case TIRED:
        currentStateObject = new TiredState(this);
        break;
      case DIRTY:
        currentStateObject = new DirtyState(this);
        break;
      case BORED:
        currentStateObject = new BoredState(this);
        break;
      default: // we must have default case
        throw new IllegalStateException("Unexpected value: " + currentState);
    }

    // The logic of the critical state, should notify the user to take care of the pet
    // given the condition that the state is changed and not in the happy state
    // because the happy state is not a critical state
    if (newState != previousState && !(currentStateObject instanceof HappyState)) {
      // should notify the user to take care of the pet, or the health of pet will decrease
      if (criticalState != null) {
        // use thread technique to deliver the message after the happy state
        if (timeManager != null) {
          final PetState finalCriticalState = criticalState;
          final PetStateBase finalStateObject = currentStateObject;
          new Thread(() -> {
            try {
              Thread.sleep(100); // 短暂延迟，确保在happy消息之后
              PetAction requiredAction = PetAction.getActionForState(finalCriticalState);
              timeManager.notifyStateChange(String.format("%s Please %s your pet!",
                  finalStateObject.getStateMessage(),
                  requiredAction.toString().toLowerCase()));
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }).start();
        }
      }
    }
    previousState = newState;  // update the previous state
  }

  /**
   * Performs an action on the pet.
   * Actions can only be performed if the pet is not sleeping (except REST).
   *
   * @param action The action to perform
   */
  public void performAction(PetAction action) {
    if (isSleeping && action != PetAction.REST) {
      throw new IllegalStateException("Your pet is sleeping now! Can't perform actions.");
      // it seems no much change whether to put return here or not.
      // return;
    }

    // Check if one of the action is performed within 30 seconds repeatedly without
    // the necessary need to do so.
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastActionTime < 30000
        && getStateScore(action.getTargetState()) == 0) {
      throw new IllegalStateException(
          String.format("Your pet doesn't need to %s now!",
              action.toString().toLowerCase()));
    }

    PetState targetState = action.getTargetState();

    if (action == PetAction.REST) {
      handleRestAction();
    } else {
      resetState(targetState);
      showHappyState();
    }

    setLastActionTime(System.currentTimeMillis());
  }

  /**
   * To deal with the logic of showing happy state.
   * The happy state will be shown for 2 seconds.
   * An iconic notification that the user has done something good for the pet.
   * (And the user's action will be shown in the GUI message box)
   */
  private void showHappyState() {
    currentStateObject = new HappyState(this);

    new Thread(() -> {
      try {
        Thread.sleep(2000);  // present happy.png for 2 seconds
        if (!isSleeping) {
          updateCurrentState();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }).start();
  }

  /**
   * To deal with the logic of waking up.
   */
  public void wakeUp() {
    if (isSleeping) {
      setSleeping(false);
      currentState = PetState.NORMAL;
      currentStateObject = new NormalState(this);
      /*
      if (currentStateObject != null) {
        currentStateObject.onWakeUp();
        // we can trace the logic of onWakeUp() in the NormalState.java
      }
      */
    }
  }

  /**
   * To deal with the logic of resting.
   */
  private void handleRestAction() {
    // check if the pet needs to rest
    if (getStateScore(PetState.TIRED) == 0) {
      throw new IllegalStateException("Your pet doesn't need to rest now!");
    }

    // the rule: if there is any critical state, the pet cannot rest
    for (PetState state : stateScores.keySet()) {
      if (state != PetState.TIRED && stateScores.get(state) >= MAX_SCORE) {
        String message = String.format("Please %s your pet first!",
            PetAction.getActionForState(state).toString().toLowerCase());
        throw new IllegalStateException(message);
      }
    }

    // reset the tired state and set the pet to sleeping
    setSleeping(true);
    resetState(PetState.TIRED);
    currentStateObject = new SleepingState(this);
  }

  /**
   * To get the pet's health.
   */
  public int getHealth() {
    return health;
  }

  /**
   * Sets the pet's health, ensuring it stays within the valid range.
   * Health cannot be less than 0 or greater than MAX_HEALTH.
   *
   * @param health The new health value
   */
  public void setHealth(int health) {
    this.health = Math.min(Math.max(health, 0), MAX_HEALTH);
  }

  public PetState getCurrentState() {
    return currentState;
  }

  /**
   * Get the state should be demonstrated in the GUI top-left corner.
   * Two special state: DEAD and SLEEPING, not included in the enum, but dealing the logic here.
   * DEAD: the pet is dead.
   * SLEEPING: the pet is sleeping, belong to the state: tired.
   * Except for the above 2 states, show its actual state
   *
   * @return for display, String.
   */
  public String getDisplayState() {
    if (health <= 0) {
      return "DEAD";
    }
    if (isSleeping) {
      return "SLEEPING";
    }
    return currentState.toString();
  }

  public PetStateBase getCurrentStateObject() {
    return currentStateObject;
  }

  public int getStateScore(PetState state) {
    return stateScores.getOrDefault(state, 0);
  }

  public boolean isSleeping() {
    return isSleeping;
  }

  /**
   * Sets the sleeping state of the pet.
   *
   * <p>This method transitions the pet into or out of the sleeping state.
   * When the pet enters the sleeping state:
   * <ul>
   *   <li>The current time is recorded as the last action time.</li>
   *   <li>If a TimeManager is associated with the pet, it initiates the sleeping process
   *   via {@code startSleeping()}.</li>
   * </ul>
   * When exiting the sleeping state, no additional logic is applied beyond updating the
   * sleeping status.
   *
   * @param sleeping {@code true} to set the pet to the sleeping state, {@code false} to
   *                 wake it up.
   */
  public void setSleeping(boolean sleeping) {
    // Check if the pet is transitioning into the sleeping state
    if (sleeping && !this.isSleeping) {
      this.lastActionTime = System.currentTimeMillis();
      if (timeManager != null) {
        timeManager.startSleeping();
      }
    }
    this.isSleeping = sleeping;
  }

  /**
   * Sets the last action time for the pet.
   *
   * <p>This method allows the timestamp of the last performed action to be updated manually.
   * It can be used to synchronize or reset the action timing for the pet.
   *
   * @param time The timestamp (in milliseconds since epoch) representing the last action time.
   */
  public void setLastActionTime(long time) {
    this.lastActionTime = time;
  }

  /**
   * Resets a state's score to 0 and updates current state.
   * The method is used to reset a state to 0 when the pet performs an action.
   *
   * @param state The state to reset
   */
  public void resetState(PetState state) {
    if (state != PetState.NORMAL) {
      stateScores.put(state, 0);
      //
      previousState = PetState.NORMAL;
      updateCurrentState();
    }
  }
}