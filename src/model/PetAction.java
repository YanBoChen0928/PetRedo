package model;

public enum PetAction {
    FEED(PetState.HUNGRY),
    REST(PetState.TIRED),
    CLEAN(PetState.DIRTY),
    PLAY(PetState.BORED);
    
    private final PetState targetState;
    
    PetAction(PetState targetState) {
        this.targetState = targetState;
    }
    
    public PetState getTargetState() {
        return targetState;
    }
} 