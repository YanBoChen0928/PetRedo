package test.model;

import model.Pet;
import model.PetState;
import model.PetAction;

/**
 * Test class for Pet model.
 * Tests basic pet functionality including:
 * - Initial state validation
 * - State score limits
 * - State priority system
 * - Health value boundaries
 * - Action effects
 * - Sleeping state behavior
 */
public class PetTest {
    private Pet pet;
    
    /**
     * Set up method executed before each test.
     * Creates a new Pet instance for testing.
     */
    @org.junit.Before
    public void setUp() {
        pet = new Pet();
    }
    
    /**
     * Tests the initial state of a newly created pet.
     * Verifies:
     * - Initial health is 100
     * - Initial state is NORMAL
     * - Pet is not sleeping
     */
    @org.junit.Test
    public void testInitialState() {
        // Check initial health value (100)
        org.junit.Assert.assertEquals(100, pet.getHealth());
        // Verify initial state is NORMAL
        org.junit.Assert.assertEquals(PetState.NORMAL, pet.getCurrentState());
        // Confirm pet starts awake
        org.junit.Assert.assertFalse(pet.isSleeping());
    }
    
    /**
     * Tests that state scores cannot exceed the maximum value of 10.
     * Attempts to set a state score above 10 and verifies it's capped.
     */
    @org.junit.Test
    public void testStateScoreLimit() {
        // Attempt to set score above maximum (10)
        pet.updateState(PetState.HUNGRY, 15);
        // Verify score is capped at 10
        org.junit.Assert.assertEquals(10, pet.getStateScore(PetState.HUNGRY));
    }
    
    /**
     * Tests the state priority system based on weight values.
     * Verifies that states with higher weights take precedence.
     */
    @org.junit.Test
    public void testStateWeightPriority() {
        // Set both states to critical (DIRTY weight=5, HUNGRY weight=3)
        pet.updateState(PetState.DIRTY, 10);
        pet.updateState(PetState.HUNGRY, 10);
        // Verify DIRTY state takes precedence due to higher weight
        org.junit.Assert.assertEquals(PetState.DIRTY, pet.getCurrentState());
    }
    
    /**
     * Tests the sleeping state mechanics.
     * Verifies:
     * - Pet can enter sleep state
     * - Actions are restricted during sleep
     */
    @org.junit.Test
    public void testSleepingState() {
        // Put pet to sleep
        pet.updateState(PetState.TIRED, 10);
        pet.performAction(PetAction.REST);
        // Verify pet is sleeping
        org.junit.Assert.assertTrue(pet.isSleeping());
        
        // Verify actions are restricted during sleep
        try {
            pet.performAction(PetAction.FEED);
            org.junit.Assert.fail("Expected IllegalStateException for action during sleep");
        } catch (IllegalStateException e) {
            // Expected behavior - actions should be restricted during sleep
        }
    }
    
    /**
     * Tests the health value boundaries.
     * Verifies health cannot go below 0 or above 100.
     */
    @org.junit.Test
    public void testHealthBounds() {
        // Test lower bound (0)
        pet.setHealth(-10);
        org.junit.Assert.assertEquals(0, pet.getHealth());
        
        // Test upper bound (100)
        pet.setHealth(150);
        org.junit.Assert.assertEquals(100, pet.getHealth());
    }
    
    /**
     * Tests the effects of various actions on pet states.
     * Verifies that each action properly resets its corresponding state.
     */
    @org.junit.Test
    public void testActionEffects() {
        // Test FEED action
        pet.updateState(PetState.HUNGRY, 10);
        pet.performAction(PetAction.FEED);
        org.junit.Assert.assertEquals(0, pet.getStateScore(PetState.HUNGRY));
        
        // Test CLEAN action
        pet.updateState(PetState.DIRTY, 10);
        pet.performAction(PetAction.CLEAN);
        org.junit.Assert.assertEquals(0, pet.getStateScore(PetState.DIRTY));
        
        // Test PLAY action
        pet.updateState(PetState.BORED, 10);
        pet.performAction(PetAction.PLAY);
        org.junit.Assert.assertEquals(0, pet.getStateScore(PetState.BORED));
    }
} 