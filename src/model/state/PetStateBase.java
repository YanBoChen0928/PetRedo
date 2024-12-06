package model.state;

import model.Pet;
import model.PetState;

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
} 