package test.model;

import model.PetState;

/**
 * Test class for PetState enumeration.
 * Tests the state weight system that determines:
 * - Priority of different states
 * - Rate of state score changes
 * 
 * Weight values correspond to state change rates:
 * - DIRTY (5): +5 points per 10 seconds
 * - TIRED (4): +4 points per 10 seconds
 * - HUNGRY (3): +3 points per 2 seconds
 * - BORED (2): +2 points per 5 seconds
 * - NORMAL (0): base state
 */
public class PetStateTest {
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
} 