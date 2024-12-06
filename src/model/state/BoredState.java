package model.state;

import model.Pet;
import model.PetState;

public class BoredState extends PetStateBase {
    public BoredState(Pet pet) {
        super(pet, PetState.BORED);
    }
    
    @Override
    public String getStateIcon() {
        return "bored.png";
    }
    
    @Override
    public String getStateMessage() {
        return "Your pet is bored!";
    }
} 