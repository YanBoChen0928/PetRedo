package model.state;

import model.Pet;
import model.PetState;

/**
 * It is the base abstract class for all states of the pet.
 * Has composition relationship with the Pet class.
 * Has a reference to the pet object and the state type.
 */

public abstract class PetStateBase {
  protected Pet pet;
  protected PetState stateType;

  public PetStateBase(Pet pet, PetState stateType) {
    this.pet = pet;
    this.stateType = stateType;
  }

  public abstract String getStateIcon();

  public abstract String getStateMessage();

  public PetState getStateType() {
    return stateType;
  }

  /**
   * Get the associated Pet object (for JUnit testing).
   *
   * @return the associated Pet object
   */
  public Pet getPet() {
    return pet;
  }
} 