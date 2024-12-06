package model;

public enum PetState {
    NORMAL(0),
    HUNGRY(3),
    DIRTY(5),
    TIRED(4),
    BORED(2);
    
    private final int weight;
    
    PetState(int weight) {
        this.weight = weight;
    }
    
    public int getWeight() {
        return weight;
    }
} 