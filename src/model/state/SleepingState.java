package model.state;

import model.Pet;
import model.PetState;

/**
 * Represents the sleeping state of the pet. (Not the element of the enum PetState)
 * The pet is sleeping and health is recovering.
 */

public class SleepingState extends PetStateBase {
  public SleepingState(Pet pet) {
    super(pet, PetState.TIRED);
  }

  @Override
  public String getStateIcon() {
    return "sleeping.png";
  }

  @Override
  public String getStateMessage() {
    return "Your pet is sleeping.";
  }
} 