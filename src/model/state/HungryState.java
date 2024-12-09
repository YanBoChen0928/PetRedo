package model.state;

import model.Pet;
import model.PetState;

/**
 * The hungry state of the pet. (child class of PetStateBase)
 */
public class HungryState extends PetStateBase {
  public HungryState(Pet pet) {
    super(pet, PetState.HUNGRY);
  }

  @Override
  public String getStateIcon() {
    return "hungry.png";
  }

  @Override
  public String getStateMessage() {
    return "Your pet is hungry!";
  }
} 