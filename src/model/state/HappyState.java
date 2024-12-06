package model.state;

import model.Pet;
import model.PetState;

public class HappyState extends PetStateBase {
    public HappyState(Pet pet) {
        super(pet, PetState.NORMAL); // 使用NORMAL作為基礎狀態
    }
    
    @Override
    public String getStateIcon() {
        return "happy.png";
    }
    
    @Override
    public String getStateMessage() {
        return "Your pet is happy!";
    }
} 