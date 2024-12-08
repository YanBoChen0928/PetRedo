package model;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import model.Pet;
import model.PetState;
import model.PetAction;
import model.state.HappyState;
import model.state.NormalState;
import model.TimeManager;

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
        // 創建一個 mock TimeManager 來避免實際的計時器運作
        TimeManager timeManager = new TimeManager(pet) {
            @Override
            public void notifyStateChange(String message) {
                // 測試中不需要實際發送通知
            }
            
            @Override
            public void shutdown() {
                // 測試中不需要實際的關閉操作
            }
            
            @Override
            public void restart() {
                // 測試中不需要實際的重啟操作
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
        // 測試快樂狀態的顯示和自動恢復
        pet.updateState(PetState.HUNGRY, 5);
        pet.performAction(PetAction.FEED);
        Assert.assertTrue(pet.getCurrentStateObject() instanceof HappyState);
        Thread.sleep(2100);  // 等待超過2秒
        Assert.assertTrue(pet.getCurrentStateObject() instanceof NormalState);
    }

    @Test
    public void testWakeUpBehavior() {
        // 測試喚醒功能
        pet.updateState(PetState.TIRED, 10);
        pet.performAction(PetAction.REST);
        Assert.assertTrue(pet.isSleeping());
        pet.wakeUp();
        Assert.assertFalse(pet.isSleeping());
        Assert.assertEquals(PetState.NORMAL, pet.getCurrentState());
    }

    @Test
    public void testMultipleCriticalStates() {
        // 測試多個危急狀態的優先級
        pet.updateState(PetState.HUNGRY, 10);
        pet.updateState(PetState.DIRTY, 10);
        pet.updateState(PetState.TIRED, 10);
        Assert.assertEquals(PetState.DIRTY, pet.getCurrentState());
    }

    @Test
    public void testHandleRestActionWithOtherCriticalStates() {
        // 測試在其他狀態危急時嘗試休息
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
        // 測試狀態分數的邊界值
        pet.updateState(PetState.HUNGRY, -1);
        Assert.assertEquals(0, pet.getStateScore(PetState.HUNGRY));

        pet.updateState(PetState.HUNGRY, 15);
        Assert.assertEquals(10, pet.getStateScore(PetState.HUNGRY));
    }

    /**
     * Tests the display state under different conditions:
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
     * Tests that rest action throws exception when pet is not tired
     */
    @Test
    public void testRestActionWithNoTiredness() {
        // 确保宠物没有疲劳值
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
     * Tests that rest action works when pet is tired
     */
    @Test
    public void testRestActionWithTiredness() {
        // 设置疲劳值
        pet.updateState(PetState.TIRED, 5);
        
        // 执行休息动作应该成功
        pet.performAction(PetAction.REST);
        
        // 验证宠物进入睡眠状态
        Assert.assertTrue(pet.isSleeping());
    }

} 