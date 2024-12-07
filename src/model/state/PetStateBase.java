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

    /**
     * 获取与该状态关联的宠物对象
     * @return 关联的Pet对象
     */
    public Pet getPet() {
        return pet;
    }
    /**
     * 当宠物从睡眠状态醒来时调用
     */
    public void onWakeUp() {
        // 默认实现为空，子类可以根据需要重写
    }
} 