package controller;

import model.Pet;
import model.PetAction;
import model.PetState;
import view.PetView;

public class PetController {
    private final Pet pet;
    private final PetView view;

    public PetController(Pet pet, PetView view) {
        this.pet = pet;
        this.view = view;
        initializeController();
    }

    private void initializeController() {
        view.setFeedButtonListener(e -> handleAction(PetAction.FEED));
        view.setCleanButtonListener(e -> handleAction(PetAction.CLEAN));
        view.setPlayButtonListener(e -> handleAction(PetAction.PLAY));
        view.setRestButtonListener(e -> handleAction(PetAction.REST));
        view.setNewPetButtonListener(e -> handleNewPet());
    }

    private void handleAction(PetAction action) {
        if (pet.getHealth() <= 0) {
            view.appendMessage("Your pet has died. Please create a new pet.");
            return;
        }

        if (pet.isSleeping() && action != PetAction.REST) {
            view.appendMessage("Your pet is sleeping. Wake it up first!");
            return;
        }

        if (action == PetAction.REST) {
            handleRestAction();
            return;
        }

        try {
            pet.performAction(action);
            updateView();
            if (pet.getStateScore(action.getTargetState()) == 0) {
                view.appendMessage(String.format("Your pet is happy after %s!", 
                    action.toString().toLowerCase()));
            } else {
                view.appendMessage(String.format("Performed %s.", 
                    action.toString().toLowerCase()));
            }
        } catch (IllegalStateException e) {
            view.appendMessage(e.getMessage());
        }
    }

    private void handleRestAction() {
        try {
            if (pet.isSleeping()) {
                pet.wakeUp();
                view.updateRestButton(false);
                view.appendMessage("Your pet woke up!");
                updateView();
                return;
            }
            
            PetState currentState = pet.getCurrentState();
            if (currentState != PetState.NORMAL && currentState != PetState.TIRED) {
                PetAction requiredAction = PetAction.getActionForState(currentState);
                view.appendMessage(String.format("Please %s your pet first!", 
                    requiredAction.toString().toLowerCase()));
                return;
            }
            
            if (currentState == PetState.TIRED) {
                pet.performAction(PetAction.REST);
                view.updateRestButton(true);
                view.appendMessage("Your pet is sleeping.");
                updateView();
            } else {
                view.appendMessage("Your pet is not tired!");
            }
        } catch (IllegalStateException e) {
            view.appendMessage(e.getMessage());
        }
    }

    private void handleNewPet() {
        if (pet.getHealth() <= 0) {
            pet.setHealth(Pet.MAX_HEALTH);
            for (PetState state : PetState.values()) {
                if (state != PetState.NORMAL) {
                    pet.resetState(state);
                }
            }
            pet.setSleeping(false);
            view.enableActionButtons();
            view.updateRestButton(false);
            view.updatePetIcon("normal");
            updateView();
            view.appendMessage("Created a new pet!");
        }
    }

    public void updateView() {
        view.updateHealth(pet.getHealth());
        
        if (pet.getHealth() <= 0) {
            view.updateState("DEAD");
            view.updatePetIcon("dead");
            view.appendMessage("Your pet has died. Please create a new pet.");
            return;
        }
        
        view.updateState(pet.getCurrentState().toString());
        view.updatePetIcon(pet.getCurrentStateObject().getStateIcon());

        for (PetState state : PetState.values()) {
            if (state != PetState.NORMAL) {
                view.updateStateScore(state, pet.getStateScore(state));
            }
        }
    }
}