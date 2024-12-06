package model.state;

import model.Pet;
import model.PetState;

public class NormalState extends PetStateBase {
    public NormalState(Pet pet) {
        super(pet, PetState.NORMAL);
    }
    
    @Override
    public String getStateIcon() {
        return "normal.png";
    }
    
    @Override
    public String getStateMessage() {
        return "Your pet is feeling normal.";
    }
} 