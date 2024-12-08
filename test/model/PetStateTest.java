package model;

import model.state.*;

/**
 * Test class for PetState enumeration and state classes.
 * Tests include:
 * 1. State Weight System:
 *    - Priority of different states
 *    - Rate of state score changes
 *    Weight values correspond to state change rates:
 *    - DIRTY (5): +5 points per 10 seconds
 *    - TIRED (4): +4 points per 10 seconds
 *    - HUNGRY (3): +3 points per 2 seconds
 *    - BORED (2): +2 points per 5 seconds
 *    - NORMAL (0): base state
 * 
 * 2. State Functionality:
 *    - State icons
 *    - State messages
 *    - State types
 *    - Pet associations
 */
public class PetStateTest {
    private Pet pet;
    private PetStateBase normalState;
    private PetStateBase hungryState;
    private PetStateBase tiredState;
    private PetStateBase dirtyState;
    private PetStateBase boredState;
    private PetStateBase happyState;
    private PetStateBase sleepingState;

    @org.junit.Before
    public void setUp() {
        pet = new Pet();
        normalState = new NormalState(pet);
        hungryState = new HungryState(pet);
        tiredState = new TiredState(pet);
        dirtyState = new DirtyState(pet);
        boredState = new BoredState(pet);
        happyState = new HappyState(pet);
        sleepingState = new SleepingState(pet);
    }

    // ========== Weight System Tests ==========
    /**
     * Tests that each state has the correct weight value.
     * Weight values determine both state priority and score increase rates.
     */
    @org.junit.Test
    public void testStateWeights() {
        // Test weight values for each state
        org.junit.Assert.assertEquals(0, PetState.NORMAL.getWeight());  // Base state
        org.junit.Assert.assertEquals(3, PetState.HUNGRY.getWeight());  // +3 per 2s
        org.junit.Assert.assertEquals(5, PetState.DIRTY.getWeight());   // +5 per 10s
        org.junit.Assert.assertEquals(4, PetState.TIRED.getWeight());   // +4 per 10s
        org.junit.Assert.assertEquals(2, PetState.BORED.getWeight());   // +2 per 5s
    }

    /**
     * Tests the priority system based on state weights.
     * Higher weight states should take precedence over lower weight states.
     * Priority order: DIRTY > TIRED > HUNGRY > BORED > NORMAL
     */
    @org.junit.Test
    public void testWeightPriority() {
        // Test DIRTY has highest priority
        org.junit.Assert.assertTrue("Cleanliness should take priority over hunger", 
            PetState.DIRTY.getWeight() > PetState.HUNGRY.getWeight());
        org.junit.Assert.assertTrue("Cleanliness should take priority over tiredness", 
            PetState.DIRTY.getWeight() > PetState.TIRED.getWeight());
            
        // Test TIRED has second highest priority
        org.junit.Assert.assertTrue("Tiredness should take priority over hunger", 
            PetState.TIRED.getWeight() > PetState.HUNGRY.getWeight());
            
        // Test HUNGRY has priority over BORED
        org.junit.Assert.assertTrue("Hunger should take priority over boredom", 
            PetState.HUNGRY.getWeight() > PetState.BORED.getWeight());
    }

    // ========== State Functionality Tests ==========
    // Normal State Tests
    @org.junit.Test
    public void testNormalStateIcon() {
        org.junit.Assert.assertEquals("normal.png", normalState.getStateIcon());
    }

    @org.junit.Test
    public void testNormalStateMessage() {
        org.junit.Assert.assertEquals("Your pet is feeling normal.", normalState.getStateMessage());
    }

    @org.junit.Test
    public void testNormalStateType() {
        org.junit.Assert.assertEquals(PetState.NORMAL, normalState.getStateType());
    }

    // Hungry State Tests
    @org.junit.Test
    public void testHungryStateIcon() {
        org.junit.Assert.assertEquals("hungry.png", hungryState.getStateIcon());
    }

    @org.junit.Test
    public void testHungryStateMessage() {
        org.junit.Assert.assertEquals("Your pet is hungry!", hungryState.getStateMessage());
    }

    @org.junit.Test
    public void testHungryStateType() {
        org.junit.Assert.assertEquals(PetState.HUNGRY, hungryState.getStateType());
    }

    // Tired State Tests
    @org.junit.Test
    public void testTiredStateIcon() {
        org.junit.Assert.assertEquals("tired.png", tiredState.getStateIcon());
    }

    @org.junit.Test
    public void testTiredStateMessage() {
        org.junit.Assert.assertEquals("Your pet is tired!", tiredState.getStateMessage());
    }

    @org.junit.Test
    public void testTiredStateType() {
        org.junit.Assert.assertEquals(PetState.TIRED, tiredState.getStateType());
    }

    // Dirty State Tests
    @org.junit.Test
    public void testDirtyStateIcon() {
        org.junit.Assert.assertEquals("dirty.png", dirtyState.getStateIcon());
    }

    @org.junit.Test
    public void testDirtyStateMessage() {
        org.junit.Assert.assertEquals("Your pet needs cleaning!", dirtyState.getStateMessage());
    }

    @org.junit.Test
    public void testDirtyStateType() {
        org.junit.Assert.assertEquals(PetState.DIRTY, dirtyState.getStateType());
    }

    // Bored State Tests
    @org.junit.Test
    public void testBoredStateIcon() {
        org.junit.Assert.assertEquals("bored.png", boredState.getStateIcon());
    }

    @org.junit.Test
    public void testBoredStateMessage() {
        org.junit.Assert.assertEquals("Your pet is bored!", boredState.getStateMessage());
    }

    @org.junit.Test
    public void testBoredStateType() {
        org.junit.Assert.assertEquals(PetState.BORED, boredState.getStateType());
    }

    // Happy State Tests
    @org.junit.Test
    public void testHappyStateIcon() {
        org.junit.Assert.assertEquals("happy.png", happyState.getStateIcon());
    }

    @org.junit.Test
    public void testHappyStateMessage() {
        org.junit.Assert.assertEquals("Your pet is happy!", happyState.getStateMessage());
    }

    @org.junit.Test
    public void testHappyStateType() { // HappyState is based on NORMAL
        org.junit.Assert.assertEquals(PetState.NORMAL, happyState.getStateType());
    }

    // Sleeping State Tests
    @org.junit.Test
    public void testSleepingStateIcon() {
        org.junit.Assert.assertEquals("sleeping.png", sleepingState.getStateIcon());
    }

    @org.junit.Test
    public void testSleepingStateMessage() {
        org.junit.Assert.assertEquals("Your pet is sleeping.", sleepingState.getStateMessage());
    }

    @org.junit.Test
    public void testSleepingStateType() { // be careful: SleepingState is based on TIRED
        org.junit.Assert.assertEquals(PetState.TIRED, sleepingState.getStateType());
    }

    // General State Tests
    @org.junit.Test
    public void testStateAssociations() {
        org.junit.Assert.assertSame(pet, normalState.getPet());
        org.junit.Assert.assertSame(pet, hungryState.getPet());
        org.junit.Assert.assertSame(pet, tiredState.getPet());
        org.junit.Assert.assertSame(pet, dirtyState.getPet());
        org.junit.Assert.assertSame(pet, boredState.getPet());
        org.junit.Assert.assertSame(pet, happyState.getPet());
        org.junit.Assert.assertSame(pet, sleepingState.getPet());
    }
} 