package model;

/**
 * Enumerates the possible actions that can be taken on a pet.
 * Each action has a target state that the pet will transition to if the action is taken.
 */
public enum PetAction {
  FEED(PetState.HUNGRY),
  CLEAN(PetState.DIRTY),
  REST(PetState.TIRED),
  PLAY(PetState.BORED);

  private final PetState targetState;

  PetAction(PetState targetState) {
    this.targetState = targetState;
  }

  /**
   * Returns the action that corresponds to the given state.
   *
   * @param state the state to get the action for
   * @return the action that corresponds to the given state
   */
  public static PetAction getActionForState(PetState state) {
    for (PetAction action : values()) {
      if (action.getTargetState() == state) {
        return action;
      }
    }
    throw new IllegalArgumentException("No action for state: " + state);
  }

  public PetState getTargetState() {
    return targetState;
  }
} 