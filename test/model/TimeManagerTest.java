package test.model;

import model.Pet;
import model.PetState;
import model.TimeManager;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TimeManagerTest {
    private Pet pet;
    private TimeManager timeManager;
    
    @Before
    public void setUp() {
        pet = new Pet();
        timeManager = new TimeManager(pet);
    }
    
    @Test
    public void testHealthDecrease() throws InterruptedException {
        pet.updateState(PetState.HUNGRY, 10);
        int initialHealth = pet.getHealth();
        Thread.sleep(1100); // Wait for more than 1 second
        assertTrue(pet.getHealth() < initialHealth);
    }
    
    @Test
    public void testHealthIncrease() throws InterruptedException {
        pet.setHealth(50);
        Thread.sleep(1100); // Wait for more than 1 second
        assertTrue(pet.getHealth() > 50);
    }
    
    @Test
    public void testStateScoreIncrease() throws InterruptedException {
        int initialScore = pet.getStateScore(PetState.HUNGRY);
        Thread.sleep(2100); // Wait for more than 2 seconds
        assertTrue(pet.getStateScore(PetState.HUNGRY) > initialScore);
    }
    
    @Test
    public void testSleepingStateFreeze() throws InterruptedException {
        pet.updateState(PetState.TIRED, 10);
        pet.performAction(PetAction.REST);
        int hungryScore = pet.getStateScore(PetState.HUNGRY);
        Thread.sleep(2100); // Wait for more than 2 seconds
        assertEquals(hungryScore, pet.getStateScore(PetState.HUNGRY));
    }
    
    @Test
    public void testHealthRecoveryDuringSleep() throws InterruptedException {
        pet.setHealth(50);
        pet.updateState(PetState.TIRED, 10);
        pet.performAction(PetAction.REST);
        Thread.sleep(1100); // Wait for more than 1 second
        assertTrue(pet.getHealth() > 50);
    }
} 