package model.state;

import model.Pet;
import model.PetState;

/**
 * The dirty state of the pet. (child class of PetStateBase)
 */
public class DirtyState extends PetStateBase {
  public DirtyState(Pet pet) {
    super(pet, PetState.DIRTY);
  }

  @Override
  public String getStateIcon() {
    return "dirty.png";
  }

  @Override
  public String getStateMessage() {
    return "Your pet is dirty!";
  }
}