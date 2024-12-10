package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
   * Set up the environment for TimeManager Test.
   */
  @Before
  public void setUp() {
    pet = new Pet();
    timeManager = new TimeManager(pet) {
      @Override
      public void notifyStateChange(String message) {
        // assure the status will not change after shutdown
      }
    };
    pet.setTimeManager(timeManager);
    // assure the pet and TimeManager are related to each other
    timeManager.restart();
  }

  @Test
  public void testHealthDecrease() throws InterruptedException {
    pet.updateState(PetState.HUNGRY, 10);
    int initialHealth = pet.getHealth();
    Thread.sleep(1100);
    Assert.assertTrue(pet.getHealth() <= initialHealth - 2);
  }

  @Test
  public void testHealthIncrease() throws InterruptedException {
    pet.setHealth(50);
    Thread.sleep(1100);
    Assert.assertTrue(pet.getHealth() >= 55);
  }

  /**
   * Tests hunger state increase rate.
   * Hunger should increase by 3 points every 2 seconds.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testHungerIncrease() throws InterruptedException {
    // Record initial hunger score
    int initialScore = pet.getStateScore(PetState.HUNGRY);
    // Wait for 2 seconds (+3 points)
    Thread.sleep(5100);
    Assert.assertTrue(pet.getStateScore(PetState.HUNGRY) >= initialScore + 3);
  }

  /**
   * Tests cleanliness state decrease rate.
   * Dirtiness should increase by 5 points every 10 seconds.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testCleanlinessDecrease() throws InterruptedException {
    // Record initial cleanliness score
    int initialScore = pet.getStateScore(PetState.DIRTY);
    // Wait for 15 seconds (+5 points)
    Thread.sleep(15100);
    Assert.assertTrue(pet.getStateScore(PetState.DIRTY) >= initialScore + 5);
  }

  /**
   * Tests tiredness increase rate.
   * Tiredness should increase by 4 points every 10 seconds.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testTirednessIncrease() throws InterruptedException {
    // Record initial tiredness score
    int initialScore = pet.getStateScore(PetState.TIRED);
    // Wait for 15 seconds (+4 points)
    Thread.sleep(15100);
    Assert.assertTrue(pet.getStateScore(PetState.TIRED) >= initialScore + 4);
  }

  /**
   * Tests boredom increase rate.
   * Boredom should increase by 2 points every 5 seconds.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testBoredIncrease() throws InterruptedException {
    // Record initial boredom score
    int initialScore = pet.getStateScore(PetState.BORED);
    // Wait for 10 seconds (+2 points)
    Thread.sleep(10100);
    Assert.assertTrue(pet.getStateScore(PetState.BORED) >= initialScore + 2);
  }

  /**
   * Tests that states don't change during sleep.
   * All state scores should remain constant while pet is sleeping.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testSleepingStateFreeze() throws InterruptedException {

    // confirm pet and TimeManager are related to each other !? important
    pet.setTimeManager(timeManager);
    // Put pet to sleep
    pet.updateState(PetState.TIRED, 10);
    pet.performAction(PetAction.REST);
    // confirm pet is sleeping
    Assert.assertTrue(pet.isSleeping());
    // Wait and verify score hasn't changed,
    // initially if no sleep should increase by 3 points every 5 seconds
    // Record hunger score before sleep
    int hungryScore = pet.getStateScore(PetState.HUNGRY);
    Thread.sleep(15100);
    Assert.assertEquals(
        hungryScore, pet.getStateScore(PetState.HUNGRY));
  }

  /**
   * Tests that the update listener is properly notified of state changes.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testUpdateListenerNotification() throws InterruptedException {
    final boolean[] listenerCalled = {false};
    timeManager.setUpdateListener(() -> listenerCalled[0] = true);

    // ensure pet and TimeManager are related to each other
    pet.setTimeManager(timeManager);

    // set a state score close to critical
    pet.updateState(PetState.HUNGRY, 9);  // set a number near the critical value

    // Wait for state update
    Thread.sleep(3100);  // 等待3.1

    Assert.assertTrue("Update listener should be called", listenerCalled[0]);
  }

  /**
   * Tests that the TimeManager properly shuts down and stops updating states.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testShutdownBehavior() throws InterruptedException {
    timeManager.shutdown();
    // assure the status will not change after shutdown
    int initialScore = pet.getStateScore(PetState.HUNGRY);
    Thread.sleep(5100);
    Assert.assertEquals(initialScore, pet.getStateScore(PetState.HUNGRY));
  }

  /**
   * Tests health recovery during sleep.
   * Health should increase while pet is sleeping.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testHealthRecoveryDuringSleep() throws InterruptedException {
    // set health to a low value 50
    pet.setHealth(50);
    // assure pet and TimeManager are related to each other
    pet.setTimeManager(timeManager);
    // let pet sleep
    pet.updateState(PetState.TIRED, 10);
    pet.performAction(PetAction.REST);

    // confirm pet is sleeping so that health can increase
    Assert.assertTrue(pet.isSleeping());

    // wait 1.1 seconds
    Thread.sleep(1100);

    // verify health has increased
    Assert.assertTrue(pet.getHealth() >= 55);
    // should be at least 55 because of health recovery by 5 per second
  }

  /**
   * Tests automatic wake up functionality.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testWakeUpFunctionality() throws InterruptedException {
    // assure pet and TimeManager are related to each other
    pet.setTimeManager(timeManager);

    // assure the pet is on the stage of critical to rest
    pet.updateState(PetState.TIRED, 10);
    pet.performAction(PetAction.REST);

    // assure pet is sleeping
    Assert.assertTrue(pet.isSleeping());

    // wake up the pet
    pet.wakeUp();

    // 验证宠物已经醒来
    Assert.assertFalse(pet.isSleeping());
    Assert.assertEquals(PetState.NORMAL, pet.getCurrentState());
  }

  /**
   * Tests automatic wake up after 60 seconds of sleep.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testAutoWakeUpAfterSixtySeconds() throws InterruptedException {
    // 确保pet和TimeManager关联
    pet.setTimeManager(timeManager);

    // 让宠物进入睡眠状态
    pet.updateState(PetState.TIRED, 10);
    pet.performAction(PetAction.REST);

    // 确认宠物在睡眠状态
    Assert.assertTrue(pet.isSleeping());

    // 设置一个监听器来捕获状态变化
    final boolean[] wakeUpCalled = {false};
    timeManager.setUpdateListener(() -> {
      if (!pet.isSleeping()) {
        wakeUpCalled[0] = true;
      }
    });

    // 等待足够长的时间（比如5秒）让自动唤醒发生
    Thread.sleep(65000);

    // 验证宠物已经自动醒来
    Assert.assertFalse(pet.isSleeping());
    Assert.assertTrue(wakeUpCalled[0]);
    Assert.assertEquals(PetState.NORMAL, pet.getCurrentState());
  }

  /**
   * Tests that pet stays asleep before 60 seconds.
   *
   * @throws InterruptedException if sleep is interrupted
   */
  @Test
  public void testPetStaysAsleepBefore60Seconds() throws InterruptedException {
    // 确保pet和TimeManager关联
    pet.setTimeManager(timeManager);

    // 让宠物进入睡眠状态
    pet.updateState(PetState.TIRED, 10);
    pet.performAction(PetAction.REST);

    // 确认宠物在睡眠状态
    Assert.assertTrue(pet.isSleeping());

    // 等待一个较短的时间（比如30秒）
    Thread.sleep(30000);

    // 验证宠物仍在睡眠
    Assert.assertTrue(pet.isSleeping());
    Assert.assertEquals(PetState.TIRED, pet.getCurrentState());
  }
} 