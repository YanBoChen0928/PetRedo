package controller;

import model.Pet;
import model.PetAction;
import model.PetState;
import view.PetView;

/**
 * Controller component of the MVC architecture.
 * Handles user interactions and updates model and view accordingly.
 */
public class PetController {
    private final Pet pet;
    private final PetView view;
    
    /**
     * Creates a new controller and initializes action listeners.
     * @param pet The pet model to control
     * @param view The view to update
     */
    public PetController(Pet pet, PetView view) {
        this.pet = pet;
        this.view = view;
        initializeController();
    }
    
    /**
     * Sets up action listeners for all UI controls.
     */
    private void initializeController() {
        view.setFeedButtonListener(e -> handleAction(PetAction.FEED));
        view.setCleanButtonListener(e -> handleAction(PetAction.CLEAN));
        view.setPlayButtonListener(e -> handleAction(PetAction.PLAY));
        view.setRestButtonListener(e -> handleAction(PetAction.REST));
        view.setNewPetButtonListener(e -> handleNewPet());
    }
    
    /**
     * Handles pet actions with appropriate checks and feedback.
     * @param action The action to perform
     */
    private void handleAction(PetAction action) {
        // Check if pet is alive
        if (pet.getHealth() <= 0) {
            view.appendMessage("Your pet has died. Please create a new pet.");
            return;
        }
        
        // Check sleep state
        if (pet.isSleeping() && action != PetAction.REST) {
            view.appendMessage("Your pet is sleeping. Wake it up first!");
            return;
        }
        
        // Handle rest action separately
        if (action == PetAction.REST) {
            handleRestAction();
            return;
        }
        
        // Check if action is needed
        if (pet.getStateScore(action.getTargetState()) < Pet.MAX_SCORE) {
            long timeSinceLastAction = System.currentTimeMillis() - pet.getLastActionTime();
            if (timeSinceLastAction < 60_000) { // 1 minutes
                view.appendMessage("There is no need to " + action.toString().toLowerCase() + " now.");
                return;
            }
        }
        
        // Perform action and update view
        pet.performAction(action);
        updateView();
        view.appendMessage("Performed " + action.toString().toLowerCase());
    }
    
    /**
     * Handles the rest/wake up action specifically.
     */
    private void handleRestAction() {
        if (pet.isSleeping()) {
            pet.setSleeping(false);
            view.updateRestButton(false);
            view.appendMessage("Your pet woke up!");
        } else if (pet.getCurrentState() == PetState.TIRED) {
            pet.performAction(PetAction.REST);
            view.updateRestButton(true);
            view.appendMessage("Your pet is sleeping.");
        } else {
            view.appendMessage("Your pet is not tired!");
        }
    }
    
    /**
     * Handles creating a new pet after death.
     */
    private void handleNewPet() {
        if (pet.getHealth() <= 0) {
            pet.setHealth(Pet.MAX_HEALTH);
            view.enableActionButtons();
            updateView();
            view.appendMessage("Created a new pet!");
        }
    }
    
    /**
     * Updates all view components to reflect current model state.
     */
    private void updateView() {
        view.updateHealth(pet.getHealth());
        view.updateState(pet.getCurrentState().toString());
        view.updatePetIcon(pet.getCurrentStateObject().getStateIcon());
    }
} 