package model.state;

import model.Pet;
import model.PetState;

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
        return "Your pet needs cleaning!";
    }
} 