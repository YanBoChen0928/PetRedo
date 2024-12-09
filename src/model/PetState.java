package model;

/**
 * Enumerates the possible states that a pet can be in.
 * Each state has a weight that determines how much it affects the pet's overall happiness.
 */
public enum PetState {
  NORMAL(0),
  HUNGRY(3),
  DIRTY(5),
  TIRED(4),
  BORED(2);

  // The weight of the state,
  // weight represent the difference will increase of pet's status score.
  private final int weight;

  PetState(int weight) {
    this.weight = weight;
  }

  public int getWeight() {
    return weight;
  }
} 