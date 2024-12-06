package model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages time-based events and state updates for the pet.
 * Handles automatic health changes and state score increases.
 */
public class TimeManager {
    private final Pet pet;
    private final ScheduledExecutorService scheduler;
    private long sleepStartTime;  // 添加睡眠開始時間
    
    // One game day equals 1 minute real time
    private static final long DAY_DURATION = 60_000;
    
    /**
     * Creates a new TimeManager for the specified pet.
     * Initializes schedulers for health and state updates.
     * @param pet The pet to manage
     */
    public TimeManager(Pet pet) {
        this.pet = pet;
        this.scheduler = Executors.newScheduledThreadPool(1);
        initializeTimers();
    }
    
    /**
     * Sets up periodic tasks for updating pet status.
     * - Health updates every second
     * - State updates at different intervals based on state type
     */
    private void initializeTimers() {
        // Health update every second
        scheduler.scheduleAtFixedRate(this::updateHealth, 1, 1, TimeUnit.SECONDS);
        
        // State updates at different intervals
        scheduler.scheduleAtFixedRate(() -> updateState(PetState.HUNGRY), 0, 2, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> updateState(PetState.DIRTY), 0, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> updateState(PetState.TIRED), 0, 10, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> updateState(PetState.BORED), 0, 5, TimeUnit.SECONDS);
        
        // 檢查睡眠時間
        scheduler.scheduleAtFixedRate(this::checkSleepTime, 0, 1, TimeUnit.SECONDS);
    }
    
    /**
     * Updates pet's health based on current state.
     * - Decreases health if in critical state
     * - Increases health if normal and below maximum
     * - Increases health while sleeping if below maximum
     */
    private void updateHealth() {
        if (pet.isSleeping()) {
            if (pet.getHealth() < Pet.MAX_HEALTH) {
                pet.setHealth(Math.min(pet.getHealth() + Pet.HEALTH_RECOVERY_RATE, Pet.MAX_HEALTH));
            }
            return;
        }
        
        if (pet.getCurrentState() != PetState.NORMAL) {
            pet.setHealth(pet.getHealth() - Pet.HEALTH_DECREASE_RATE);
        } else if (pet.getHealth() < Pet.MAX_HEALTH) {
            pet.setHealth(Math.min(pet.getHealth() + Pet.HEALTH_RECOVERY_RATE, Pet.MAX_HEALTH));
        }
    }
    
    /**
     * Updates a specific state's score.
     * Skips update if pet is sleeping (except for TIRED state).
     * @param state The state to update
     */
    private void updateState(PetState state) {
        if (pet.isSleeping()) {
            return; // 睡眠時不更新任何狀態
        }
        
        int currentScore = pet.getStateScore(state);
        int increment = state.getWeight();
        int newScore = Math.min(currentScore + increment, Pet.MAX_SCORE);
        
        if (newScore != currentScore) {
            pet.updateState(state, newScore);
        }
    }
    
    /**
     * Shuts down the scheduler when program ends.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
    
    private void checkSleepTime() {
        if (pet.isSleeping()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - sleepStartTime >= 60_000) {
                pet.setSleeping(false);
                pet.updateCurrentState();
            }
        }
    }
    
    public void startSleeping() {
        sleepStartTime = System.currentTimeMillis();
    }
} 