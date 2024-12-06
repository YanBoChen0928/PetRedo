package test.model;

import model.PetState;
import org.junit.Test;
import static org.junit.Assert.*;

public class PetStateTest {
    @Test
    public void testStateWeights() {
        assertEquals(0, PetState.NORMAL.getWeight());
        assertEquals(3, PetState.HUNGRY.getWeight());
        assertEquals(5, PetState.DIRTY.getWeight());
        assertEquals(4, PetState.TIRED.getWeight());
        assertEquals(2, PetState.BORED.getWeight());
    }
    
    @Test
    public void testWeightComparison() {
        assertTrue(PetState.DIRTY.getWeight() > PetState.HUNGRY.getWeight());
        assertTrue(PetState.TIRED.getWeight() > PetState.BORED.getWeight());
        assertTrue(PetState.HUNGRY.getWeight() > PetState.BORED.getWeight());
    }
} 