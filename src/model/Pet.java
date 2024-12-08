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
    private PetState previousState = PetState.NORMAL;  // 添加前一个状态的记录
    
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
    
    public TimeManager getTimeManager() {
        return timeManager;
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
     * @param state The state to update
     * @param score The new score value (will be capped at MAX_SCORE)
     */
    public void updateState(PetState state, int score) {
        if (health <= 0) {  // 如果已經死亡，不做任何更新
            return;
        }
        
        if (state != PetState.NORMAL) {
            //restrict the score to be within 0 and MAX_SCORE
            int currentScore = Math.min(Math.max(score,0), MAX_SCORE);
            stateScores.put(state, currentScore);
            if (currentScore >= MAX_SCORE) {
                // 使用 PetAction 來獲取對應的動作訊息
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
            currentState = PetState.TIRED;  // 睡眠时仍然显示为TIRED状态
            currentStateObject = new SleepingState(this);
            return;
        }
        
        PetState criticalState = null;
        int maxWeight = 0;
        
        // 找出权重最高的临界状态
        for (PetState state : stateScores.keySet()) {
            if (stateScores.get(state) >= MAX_SCORE) {
                if (state.getWeight() > maxWeight) {
                    maxWeight = state.getWeight();
                    criticalState = state;
                }
            }
        }
        
        PetState newState = (criticalState != null) ? criticalState : PetState.NORMAL;
        this.currentState = newState;
        
        // 先更新状态对象
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
        
        // 检查状态是否发生变化，并且不是从happy状态切换
        if (newState != previousState && !(currentStateObject instanceof HappyState)) {
            // 如果是临界状态，显示警告消息
            if (criticalState != null) {
                // 使用新的延迟通知方法
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
        previousState = newState;  // 更新前一个状态
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

    /**
     * Sets the pet's health, ensuring it stays within the valid range.
     * Health cannot be less than 0 or greater than MAX_HEALTH.
     * @param health The new health value
     */
    public void setHealth(int health) {
        this.health = Math.min(Math.max(health, 0), MAX_HEALTH);
    }
    
    public PetState getCurrentState() { return currentState; }
    
    /**
     * 获取用于显示的状态文本
     * 当宠物死亡时返回"DEAD"
     * 当宠物在睡眠时返回"SLEEPING"
     * 否则返回实际状态
     * @return 显示用的状态文本
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
            // 强制更新前一个状态为NORMAL，这样其他临界状态的消息会被显示
            previousState = PetState.NORMAL;
            updateCurrentState();
        }
    }
} 