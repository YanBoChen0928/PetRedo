package model.state;

import model.Pet;
import model.PetState;

public class TiredState extends PetStateBase {
    public TiredState(Pet pet) {
        super(pet, PetState.TIRED);
    }
    
    @Override
    public String getStateIcon() {
        return "tired.png";
    }
    
    @Override
    public String getStateMessage() {
        return "Your pet is tired!";
    }
} 