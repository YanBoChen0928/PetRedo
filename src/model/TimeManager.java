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
    
    // One game day equals 10 minutes real time
    private static final long DAY_DURATION = 600_000;
    
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
        // Skip updates while sleeping (except TIRED)
        if (pet.isSleeping() && state != PetState.TIRED) {
            return;
        }
        
        int currentScore = pet.getStateScore(state);
        int increment = state.getWeight();
        pet.updateState(state, currentScore + increment);
    }
    
    /**
     * Shuts down the scheduler when program ends.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
} 