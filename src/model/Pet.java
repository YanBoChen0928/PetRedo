package model;

import java.util.concurrent.ConcurrentHashMap;
import model.state.BoredState;
import model.state.DirtyState;
import model.state.HungryState;
import model.state.NormalState;
import model.state.PetStateBase;
import model.state.TiredState;

/**
 * Represents a virtual pet with various states and attributes.
 * The pet has health, different states (hungry, tired, etc.), and can perform various actions.
 */
public class Pet {
    // Core attributes
    private int health;
    private ConcurrentHashMap<PetState, Integer> stateScores;
    private PetState currentState;
    private boolean isSleeping;
    private long lastActionTime;
    private PetStateBase currentStateObject;
    
    // Constants for pet attributes
    public static final int MAX_HEALTH = 100;
    public static final int MAX_SCORE = 10;
    public static final int HEALTH_DECREASE_RATE = 5;
    public static final int HEALTH_RECOVERY_RATE = 15;
    
    /**
     * Creates a new pet with default values.
     * Initial health is set to maximum, state is normal, and all state scores are 0.
     */
    public Pet() {
        this.health = MAX_HEALTH;
        this.stateScores = new ConcurrentHashMap<>();
        this.currentState = PetState.NORMAL;
        this.isSleeping = false;
        initializeStates();
        this.currentStateObject = new NormalState(this);
    }
    
    /**
     * Initializes all possible states with score 0.
     * Sets the initial timestamp for action tracking.
     */
    private void initializeStates() {
        for (PetState state : PetState.values()) {
            if (state != PetState.NORMAL) {
                stateScores.put(state, 0);
            }
        }
        this.lastActionTime = System.currentTimeMillis();
    }
    
    /**
     * Updates the score for a specific state and recalculates current state.
     * @param state The state to update
     * @param score The new score value (will be capped at MAX_SCORE)
     */
    public void updateState(PetState state, int score) {
        if (state != PetState.NORMAL) {
            int currentScore = Math.min(score, MAX_SCORE);
            stateScores.put(state, currentScore);
            updateCurrentState();
        }
    }
    
    /**
     * Updates the current state based on state scores and weights.
     * Chooses the state with highest weight among critical states (score = MAX_SCORE).
     */
    private void updateCurrentState() {
        PetState criticalState = null;
        int maxWeight = 0;
        
        // Find the critical state with highest weight
        for (PetState state : stateScores.keySet()) {
            if (stateScores.get(state) >= MAX_SCORE) {
                if (state.getWeight() > maxWeight) {
                    maxWeight = state.getWeight();
                    criticalState = state;
                }
            }
        }
        
        this.currentState = (criticalState != null) ? criticalState : PetState.NORMAL;
        updateStateObject();
    }
    
    /**
     * Updates the state object based on current state.
     * Creates new state object to handle state-specific behavior.
     */
    private void updateStateObject() {
        switch(currentState) {
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
        }
    }
    
    /**
     * Performs an action on the pet.
     * Actions can only be performed if the pet is not sleeping (except REST).
     * @param action The action to perform
     */
    public void performAction(PetAction action) {
        // Cannot perform actions while sleeping (except REST)
        if (isSleeping && action != PetAction.REST) {
            return;
        }
        
        PetState targetState = action.getTargetState();
        
        if (action == PetAction.REST) {
            handleRestAction();
        } else {
            resetState(targetState);
        }
        
        setLastActionTime(System.currentTimeMillis());
    }
    
    /**
     * Handles the REST action specifically.
     * If pet is tired, it will enter sleep mode for 1 minute.
     */
    private void handleRestAction() {
        if (currentState == PetState.TIRED) {
            setSleeping(true);
            resetState(PetState.TIRED);
            // Auto wake up after 1 minute
            new Thread(() -> {
                try {
                    Thread.sleep(60000);
                    if (isSleeping) {
                        setSleeping(false);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    // Getters and setters with validation
    public int getHealth() { return health; }
    
    public void setHealth(int health) {
        this.health = Math.min(Math.max(health, 0), MAX_HEALTH);
    }
    
    public PetState getCurrentState() { return currentState; }
    
    public PetStateBase getCurrentStateObject() { return currentStateObject; }
    
    public int getStateScore(PetState state) {
        return stateScores.getOrDefault(state, 0);
    }
    
    public boolean isSleeping() { return isSleeping; }
    
    public void setSleeping(boolean sleeping) { this.isSleeping = sleeping; }
    
    public long getLastActionTime() { return lastActionTime; }
    
    public void setLastActionTime(long time) { this.lastActionTime = time; }
    
    /**
     * Resets a state's score to 0 and updates current state.
     * @param state The state to reset
     */
    public void resetState(PetState state) {
        if (state != PetState.NORMAL) {
            stateScores.put(state, 0);
            updateCurrentState();
        }
    }
} 