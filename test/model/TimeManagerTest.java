package model;

import model.Pet;
import model.PetState;
import model.PetAction;
import model.TimeManager;

/**
 * Test class for TimeManager.
 * Tests the time-based mechanics of the pet simulation including:
 * - Health value changes over time
 * - State score increases
 * - Sleep state effects
 * All timings are based on the specification:
 * 1 game day = 1 real minute (60,000ms)
 */
public class TimeManagerTest {
    private Pet pet;
    private TimeManager timeManager;
    
    /**
     * Set up method executed before each test.
     * Creates new Pet and TimeManager instances for testing.
     */
    @org.junit.Before
    public void setUp() {
        pet = new Pet();
        timeManager = new TimeManager(pet);
    }
    
    /**
     * Tests health decrease in critical state.
     * Health should decrease by 5 points per second when any state is critical.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testHealthDecrease() throws InterruptedException {
        // Set pet to critical hunger state
        pet.updateState(PetState.HUNGRY, 10);
        int initialHealth = pet.getHealth();
        // Wait for health decrease (-2 per second)
        Thread.sleep(1100);
        org.junit.Assert.assertTrue(pet.getHealth() <= initialHealth - 2);
    }

    /**
     * Tests health recovery in normal state.
     * Health should increase by 15 points per second when no state is critical.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testHealthIncrease() throws InterruptedException {
        // Set initial health to test recovery
        pet.setHealth(50);
        // Wait for health increase (+5 per second)
        Thread.sleep(1100);
        org.junit.Assert.assertTrue(pet.getHealth() >= 55);
    }
    
    /**
     * Tests hunger state increase rate.
     * Hunger should increase by 3 points every 2 seconds.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testHungerIncrease() throws InterruptedException {
        // Record initial hunger score
        int initialScore = pet.getStateScore(PetState.HUNGRY);
        // Wait for 2 seconds (+3 points)
        Thread.sleep(5100);
        org.junit.Assert.assertTrue(pet.getStateScore(PetState.HUNGRY) >= initialScore + 3);
    }
    
    /**
     * Tests cleanliness state decrease rate.
     * Dirtiness should increase by 5 points every 10 seconds.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testCleanlinessDecrease() throws InterruptedException {
        // Record initial cleanliness score
        int initialScore = pet.getStateScore(PetState.DIRTY);
        // Wait for 15 seconds (+5 points)
        Thread.sleep(15100);
        org.junit.Assert.assertTrue(pet.getStateScore(PetState.DIRTY) >= initialScore + 5);
    }
    
    /**
     * Tests tiredness increase rate.
     * Tiredness should increase by 4 points every 10 seconds.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testTirednessIncrease() throws InterruptedException {
        // Record initial tiredness score
        int initialScore = pet.getStateScore(PetState.TIRED);
        // Wait for 15 seconds (+4 points)
        Thread.sleep(15100);
        org.junit.Assert.assertTrue(pet.getStateScore(PetState.TIRED) >= initialScore + 4);
    }
    
    /**
     * Tests boredom increase rate.
     * Boredom should increase by 2 points every 5 seconds.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testBoredIncrease() throws InterruptedException {
        // Record initial boredom score
        int initialScore = pet.getStateScore(PetState.BORED);
        // Wait for 10 seconds (+2 points)
        Thread.sleep(10100);
        org.junit.Assert.assertTrue(pet.getStateScore(PetState.BORED) >= initialScore + 2);
    }
    
    /**
     * Tests that states don't change during sleep.
     * All state scores should remain constant while pet is sleeping.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testSleepingStateFreeze() throws InterruptedException {
        // Record hunger score before sleep
        int hungryScore = pet.getStateScore(PetState.HUNGRY);
        // confirm pet and TimeManager are related to each other !? important
        pet.setTimeManager(timeManager);
        // Put pet to sleep
        pet.updateState(PetState.TIRED, 10);
        pet.performAction(PetAction.REST);
        // confirm pet is sleeping
        org.junit.Assert.assertTrue(pet.isSleeping());
        // Wait and verify score hasn't changed,
        // initially if no sleep should increase by 3 points every 5 seconds
        Thread.sleep(15100);
        org.junit.Assert.assertEquals(
            hungryScore, pet.getStateScore(PetState.HUNGRY));
    }
    /**
     * Tests that the update listener is properly notified of state changes.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testUpdateListenerNotification() throws InterruptedException {
        final boolean[] listenerCalled = {false};
        timeManager.setUpdateListener(() -> listenerCalled[0] = true);
        
        // ensure pet and TimeManager are related to each other
        pet.setTimeManager(timeManager);
        
        // set a state score close to critical
        pet.updateState(PetState.HUNGRY, 9);  // set a number near the critical value
        
        // Wait for state update
        Thread.sleep(3100);  // 等待3.1
        
        org.junit.Assert.assertTrue("Update listener should be called", listenerCalled[0]);
    }

    /**
     * Tests that the TimeManager properly shuts down and stops updating states.
     * @throws InterruptedException if sleep is interrupted
     */
    @org.junit.Test
    public void testShutdownBehavior() throws InterruptedException {
        timeManager.shutdown();
        // 確保在shutdown後狀態不會改變
        int initialScore = pet.getStateScore(PetState.HUNGRY);
        Thread.sleep(5100);
        org.junit.Assert.assertEquals(initialScore, pet.getStateScore(PetState.HUNGRY));
    }
} 