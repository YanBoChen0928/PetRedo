package model;

import model.state.HappyState;
import model.state.NormalState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
  @Before
  public void setUp() {
    pet = new Pet();
    // create a mock TimeManager to avoid actual time-based operations
    TimeManager timeManager = new TimeManager(pet) {
      @Override
      public void notifyStateChange(String message) {
        // during testing, we don't need to display messages
      }

      @Override
      public void shutdown() {
        // during testing, we don't need to shut down the time manager
      }

      @Override
      public void restart() {
        // during testing, we don't need to restart the time manager
      }
    };
    pet.setTimeManager(timeManager);
  }

  /**
   * Tests the initial state of a newly created pet.
   * Verifies:
   * - Initial health is 100
   * - Initial state is NORMAL
   * - Pet is not sleeping
   */
  @Test
  public void testInitialState() {
    Assert.assertEquals(100, pet.getHealth());
    Assert.assertEquals(PetState.NORMAL, pet.getCurrentState());
    Assert.assertFalse(pet.isSleeping());
  }

  /**
   * Tests that state scores cannot exceed the maximum value of 10.
   * Attempts to set a state score above 10 and verifies it's capped.
   */
  @Test
  public void testStateScoreLimit() {
    pet.updateState(PetState.HUNGRY, 15);
    Assert.assertEquals(10, pet.getStateScore(PetState.HUNGRY));
  }

  /**
   * Tests the state priority system based on weight values.
   * Verifies that states with higher weights take precedence.
   */
  @Test
  public void testStateWeightPriority() {
    pet.updateState(PetState.DIRTY, 10);  // weight = 5
    pet.updateState(PetState.HUNGRY, 10); // weight = 3
    Assert.assertEquals(PetState.DIRTY, pet.getCurrentState());
  }

  /**
   * Tests the sleeping state mechanics.
   * Verifies:
   * - Pet can enter sleep state
   * - Actions are restricted during sleep
   */
  @Test
  public void testSleepingState() {
    // Put pet to sleep
    pet.updateState(PetState.TIRED, 10);
    pet.performAction(PetAction.REST);
    // Verify pet is sleeping
    Assert.assertTrue(pet.isSleeping());

    // Verify actions are restricted during sleep
    try {
      pet.performAction(PetAction.FEED);
      Assert.fail("Expected IllegalStateException for action during sleep");
    } catch (IllegalStateException e) {
      // Expected behavior - actions should be restricted during sleep
    }
  }

  /**
   * Tests the health value boundaries.
   * Verifies health cannot go below 0 or above 100.
   */
  @Test
  public void testHealthBounds() {
    // Test lower bound (0)
    pet.setHealth(-10);
    Assert.assertEquals(0, pet.getHealth());

    // Test upper bound (100)
    pet.setHealth(150);
    Assert.assertEquals(100, pet.getHealth());
  }

  /**
   * Tests the effects of various actions on pet states.
   * Verifies that each action properly resets its corresponding state.
   */
  @Test
  public void testActionEffects() {
    // Test FEED action
    pet.updateState(PetState.HUNGRY, 10);
    pet.performAction(PetAction.FEED);
    Assert.assertEquals(0, pet.getStateScore(PetState.HUNGRY));

    // Test CLEAN action
    pet.updateState(PetState.DIRTY, 10);
    pet.performAction(PetAction.CLEAN);
    Assert.assertEquals(0, pet.getStateScore(PetState.DIRTY));

    // Test PLAY action
    pet.updateState(PetState.BORED, 10);
    pet.performAction(PetAction.PLAY);
    Assert.assertEquals(0, pet.getStateScore(PetState.BORED));
  }

  @Test
  public void testShowHappyState() throws InterruptedException {
    // test happy state about it's display time and state
    pet.updateState(PetState.HUNGRY, 5);
    pet.performAction(PetAction.FEED);
    Assert.assertTrue(pet.getCurrentStateObject() instanceof HappyState);
    Thread.sleep(2100);  // wait for 2 seconds (the time of happy state)
    Assert.assertTrue(pet.getCurrentStateObject() instanceof NormalState);
  }

  @Test
  public void testWakeUpBehavior() {
    // test wake up behavior
    pet.updateState(PetState.TIRED, 10);
    pet.performAction(PetAction.REST);
    Assert.assertTrue(pet.isSleeping());
    pet.wakeUp();
    Assert.assertFalse(pet.isSleeping());
    Assert.assertEquals(PetState.NORMAL, pet.getCurrentState());
  }

  @Test
  public void testMultipleCriticalStates() {
    // test multiple critical states whether display the highest priority state
    pet.updateState(PetState.HUNGRY, 10);
    pet.updateState(PetState.DIRTY, 10);
    pet.updateState(PetState.TIRED, 10);
    Assert.assertEquals(PetState.DIRTY, pet.getCurrentState());
  }

  @Test
  public void testHandleRestActionWithOtherCriticalStates() {
    // test rest action when other critical states are present, which is not allowed
    pet.updateState(PetState.HUNGRY, 10);
    pet.updateState(PetState.TIRED, 10);
    try {
      pet.performAction(PetAction.REST);
      Assert.fail("Should throw IllegalStateException");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("feed"));
    }
  }

  @Test
  public void testStateScoreValidation() {
    // test the state score validation
    pet.updateState(PetState.HUNGRY, -1);
    Assert.assertEquals(0, pet.getStateScore(PetState.HUNGRY));

    pet.updateState(PetState.HUNGRY, 15);
    Assert.assertEquals(10, pet.getStateScore(PetState.HUNGRY));
  }

  /**
   * Tests the display state under different conditions.
   * - Dead state (health = 0)
   * - Sleeping state
   * - Normal state
   */
  @Test
  public void testGetDisplayState() {
    // Test dead state
    pet.setHealth(0);
    Assert.assertEquals("DEAD", pet.getDisplayState());

    // Reset health and test sleeping state
    pet.setHealth(100);
    pet.updateState(PetState.TIRED, 10);
    pet.performAction(PetAction.REST);
    Assert.assertEquals("SLEEPING", pet.getDisplayState());

    // Wake up and test normal state
    pet.wakeUp();
    Assert.assertEquals("NORMAL", pet.getDisplayState());

    // Test other states
    pet.updateState(PetState.HUNGRY, 10);
    Assert.assertEquals("HUNGRY", pet.getDisplayState());
  }

  /**
   * Tests that rest action throws exception when pet is not tired.
   */
  @Test
  public void testRestActionWithNoTiredness() {
    // assure that the pet is not tired at all.
    pet.updateState(PetState.TIRED, 0);

    try {
      pet.performAction(PetAction.REST);
      Assert.fail("Should throw IllegalStateException when pet is not tired");
    } catch (IllegalStateException e) {
      Assert.assertEquals(
          "Your pet doesn't need to rest now!",
          e.getMessage()
      );
    }
  }

  /**
   * Tests that rest action works when pet is tired.
   */
  @Test
  public void testRestActionWithTiredness() {
    // set the pet to a tired score
    pet.updateState(PetState.TIRED, 5);

    // should be able to rest
    pet.performAction(PetAction.REST);

    // confirm that the pet is sleeping
    Assert.assertTrue(pet.isSleeping());
  }

} 