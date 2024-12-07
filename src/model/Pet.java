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
    // Core attributes
    private int health;
    // it's not final here, it will update and change the state scores
    private ConcurrentHashMap<PetState, Integer> stateScores;
    private PetState currentState;
    private boolean isSleeping;
    private long lastActionTime;
    private PetStateBase currentStateObject;
    
    // Constants for pet attributes
    public static final int MAX_HEALTH = 100;
    public static final int MAX_SCORE = 10;
    public static final int HEALTH_DECREASE_RATE = 2;
    public static final int HEALTH_RECOVERY_RATE = 5;
    
    private TimeManager timeManager;  // 添加 TimeManager 引用
    
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
    
    public void setTimeManager(TimeManager timeManager) {
        this.timeManager = timeManager;
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
            //restrict the score to be within 0 and MAX_SCORE
            int currentScore = Math.min(Math.max(score,0), MAX_SCORE);
            stateScores.put(state, currentScore);
            updateCurrentState();
        }
    }
    
    /**
     * Updates the current state based on state scores and weights.
     */
    public void updateCurrentState() {
        if (isSleeping) {
            currentStateObject = new SleepingState(this);
            return;
        }
        
        PetState criticalState = null;
        int maxWeight = 0;
        
        for (PetState state : stateScores.keySet()) {
            if (stateScores.get(state) >= MAX_SCORE) {
                if (state.getWeight() > maxWeight) {
                    maxWeight = state.getWeight();
                    criticalState = state;
                }
            }
        }
        
        this.currentState = (criticalState != null) ? criticalState : PetState.NORMAL;
        
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
        if (isSleeping && action != PetAction.REST) {
            throw new IllegalStateException("Your pet is sleeping now! Can't perform actions.");
            // ToDo: penidng for check
            // return;
        }
        
        // 检查是否在30秒内重复执行同一动作
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime < 30000 && 
            getStateScore(action.getTargetState()) == 0) {
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
    
    private void showHappyState() {
        currentStateObject = new HappyState(this);
        
        new Thread(() -> {
            try {
                Thread.sleep(2000);  // 从3秒改为2秒
                if (!isSleeping) {
                    updateCurrentState();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * 处理宠物从睡眠状态醒来的逻辑
     */
    public void wakeUp() {
        if (isSleeping) {
            setSleeping(false);
            currentState = PetState.NORMAL;
            currentStateObject = new NormalState(this);
            // 醒来时发出通知，让 Controller 能够更新界面和显示消息
            if (currentStateObject != null) {
                currentStateObject.onWakeUp();
            }
        }
    }
    
    private void handleRestAction() {
        // 检查是否有疲劳值
        if (getStateScore(PetState.TIRED) == 0) {
            throw new IllegalStateException("Your pet doesn't need to rest now!");
        }
        
        // 检查其他危急状态
        for (PetState state : stateScores.keySet()) {
            if (state != PetState.TIRED && stateScores.get(state) >= MAX_SCORE) {
                String message = String.format("Please %s your pet first!", 
                    PetAction.getActionForState(state).toString().toLowerCase());
                throw new IllegalStateException(message);
            }
        }
        
        // 进入睡眠状态
        setSleeping(true);
        resetState(PetState.TIRED);
        currentStateObject = new SleepingState(this);
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
    
    public void setSleeping(boolean sleeping) {
        if (sleeping && !this.isSleeping) {
            this.lastActionTime = System.currentTimeMillis();
            if (timeManager != null) {
                timeManager.startSleeping();
            }
        }
        this.isSleeping = sleeping;
    }
    
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