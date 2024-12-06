package model;

import model.Pet;
import model.PetState;
import model.PetAction;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PetTest {
    private Pet pet;
    
    @Before
    public void setUp() {
        pet = new Pet();
    }
    
    @Test
    public void testInitialState() {
        assertEquals(Pet.MAX_HEALTH, pet.getHealth());
        assertEquals(PetState.NORMAL, pet.getCurrentState());
        assertFalse(pet.isSleeping());
    }
    
    @Test
    public void testUpdateState() {
        pet.updateState(PetState.HUNGRY, 10);
        assertEquals(PetState.HUNGRY, pet.getCurrentState());
        assertEquals(10, pet.getStateScore(PetState.HUNGRY));
    }
    
    @Test
    public void testHealthBounds() {
        pet.setHealth(Pet.MAX_HEALTH + 10);
        assertEquals(Pet.MAX_HEALTH, pet.getHealth());
        
        pet.setHealth(-10);
        assertEquals(0, pet.getHealth());
    }
    
    @Test
    public void testStateScoreLimit() {
        pet.updateState(PetState.HUNGRY, Pet.MAX_SCORE + 5);
        assertEquals(Pet.MAX_SCORE, pet.getStateScore(PetState.HUNGRY));
    }
    
    @Test
    public void testStateWeightPriority() {
        // DIRTY(5) has higher weight than HUNGRY(3)
        pet.updateState(PetState.DIRTY, 10);
        pet.updateState(PetState.HUNGRY, 10);
        assertEquals(PetState.DIRTY, pet.getCurrentState());
    }
    
    @Test
    public void testPerformAction() {
        pet.updateState(PetState.HUNGRY, 10);
        pet.performAction(PetAction.FEED);
        assertEquals(0, pet.getStateScore(PetState.HUNGRY));
        assertEquals(PetState.NORMAL, pet.getCurrentState());
    }
    
    @Test
    public void testSleepingState() {
        pet.updateState(PetState.TIRED, 10);
        pet.performAction(PetAction.REST);
        assertTrue(pet.isSleeping());
        
        // Cannot perform other actions while sleeping
        pet.performAction(PetAction.FEED);
        assertEquals(0, pet.getStateScore(PetState.HUNGRY));
    }
    
    @Test
    public void testResetState() {
        pet.updateState(PetState.BORED, 10);
        pet.resetState(PetState.BORED);
        assertEquals(0, pet.getStateScore(PetState.BORED));
        assertEquals(PetState.NORMAL, pet.getCurrentState());
    }
} 