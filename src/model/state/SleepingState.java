package model.state;

import model.Pet;
import model.PetState;

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