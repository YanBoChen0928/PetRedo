package model;

public enum PetAction {
    FEED(PetState.HUNGRY),
    CLEAN(PetState.DIRTY),
    REST(PetState.TIRED),
    PLAY(PetState.BORED);
    
    private final PetState targetState;
    
    PetAction(PetState targetState) {
        this.targetState = targetState;
    }
    
    public PetState getTargetState() {
        return targetState;
    }
    
    public static PetAction getActionForState(PetState state) {
        for (PetAction action : values()) {
            if (action.getTargetState() == state) {
                return action;
            }
        }
        throw new IllegalArgumentException("No action for state: " + state);
    }
} 