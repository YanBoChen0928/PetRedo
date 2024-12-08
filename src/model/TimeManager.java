package model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manages time-based events and state updates for the pet.
 * Handles automatic health changes and state score increases.
 */
public class TimeManager {
    private final Pet pet;
    private ScheduledExecutorService scheduler;
    private long sleepStartTime;
    private Runnable updateListener;
    private Consumer<String> messageListener;
    
    // One game day equals 1 minute real time
    private static final long DAY_DURATION = 60_000;
    
    /**
     * Creates a new TimeManager for the specified pet.
     * Initializes schedulers for health and state updates.
     * @param pet The pet to manage
     */
    public TimeManager(Pet pet) {
        this.pet = pet;
        startScheduler();
    }
    
    /**
     * 启动时间管理系统
     */
    private void startScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        initializeTimers();
    }
    
    /**
     * 重新启动时间管理系统
     */
    public void restart() {
        // 重置基本属性
        pet.setHealth(Pet.MAX_HEALTH);
        pet.setSleeping(false);
        
        // 重启时间系统
        shutdown();  // 先关闭现有的scheduler
        startScheduler();  // 重新启动
    }
    
    /**
     * 关闭时间管理系统
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();  // 立即停止所有任务
            try {
                // 等待所有任务完成
                scheduler.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void setUpdateListener(Runnable listener) {
        this.updateListener = listener;
    }
    
    public void setMessageListener(Consumer<String> listener) {
        this.messageListener = listener;
    }
    
    public void notifyStateChange(String message) {
        if (messageListener != null) {
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
        }, 5 , 5, TimeUnit.SECONDS);
        
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
        
        // 檢查睡眠時間
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
            return;  // 如果宠物已死亡，不再更新健康值
        }
        
        if (pet.isSleeping()) {
            if (pet.getHealth() < Pet.MAX_HEALTH) {
                pet.setHealth(Math.min(pet.getHealth() + Pet.HEALTH_RECOVERY_RATE, Pet.MAX_HEALTH));
            }
            return;
        }
        
        if (pet.getCurrentState() != PetState.NORMAL) {
            int newHealth = pet.getHealth() - Pet.HEALTH_DECREASE_RATE;
            // 检查是否会死亡
            if (newHealth <= 0) {
                // 先关闭时间系统，确保不会有新的状态更新
                shutdown();
                // 重置所有状态分数
                for (PetState state : PetState.values()) {
                    if (state != PetState.NORMAL) {
                        pet.resetState(state);
                    }
                }
                // 设置健康值为0并显示死亡消息
                pet.setHealth(0);
                notifyStateChange("Your pet has died!");
                return;
            }
            pet.setHealth(newHealth);
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
        if (pet.getHealth() <= 0) {
            return;  // 如果宠物已死亡，不再更新状态
        }
        
        if (pet.isSleeping()) {
            return;  // 如果宠物在睡眠，不更新状态
        }
        
        int currentScore = pet.getStateScore(state);
        int increment = state.getWeight();
        int newScore = Math.min(currentScore + increment, Pet.MAX_SCORE);
        
        if (newScore != currentScore) {
            pet.updateState(state, newScore);
        }
    }
    
    private void checkSleepTime() {
        if (pet.isSleeping()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - sleepStartTime >= 60000) {
                pet.setSleeping(false);
                pet.wakeUp();
            }
        }
    }
    
    public void startSleeping() {
        sleepStartTime = System.currentTimeMillis();
    }
} 