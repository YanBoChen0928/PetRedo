package controller;

import model.Pet;
import model.PetAction;
import model.PetState;
import view.PetView;

/**
 * The controller of M-C-V pattern for the pet.
 * It plays the important role to handle the user input and update the view.
 */
public class PetController {
  private final Pet pet;
  private final PetView view;

  /**
   * Constructor of the PetController.
   *
   * @param pet  The pet model object.
   * @param view The view object.
   */
  public PetController(Pet pet, PetView view) {
    this.pet = pet;
    this.view = view;
    initializeController();
  }

  /**
   * Initialize the controller.
   * Set the listeners for the buttons in the view.
   */
  private void initializeController() {
    // lumbda expression: to set the listener for the handleAction button
    view.setFeedButtonListener(e -> handleAction(PetAction.FEED));
    view.setCleanButtonListener(e -> handleAction(PetAction.CLEAN));
    view.setPlayButtonListener(e -> handleAction(PetAction.PLAY));
    view.setRestButtonListener(e -> handleAction(PetAction.REST));
    view.setNewPetButtonListener(e -> handleNewPet());

    if (pet.getTimeManager() != null) {
      pet.getTimeManager().setMessageListener(message -> view.appendMessage(message));
    }
  }

  /**
   * Handle the action of the pet.
   *
   * @param action The action to be handled.
   */
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
      if (pet.getStateScore(action.getTargetState()) == 0) {
        view.appendMessage(String.format("Your pet is happy after %s!",
            action.toString().toLowerCase()));
      } else {
        view.appendMessage(String.format("Performed %s.",
            action.toString().toLowerCase()));
      }
      updateView();
    } catch (IllegalStateException e) {
      view.appendMessage(e.getMessage());
    }
  }

  private void handleRestAction() {
    try {
      if (pet.isSleeping()) {
        pet.wakeUp();
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

      if (pet.getStateScore(PetState.TIRED) > 0) {
        pet.performAction(PetAction.REST);
        view.appendMessage("Your pet is sleeping.");
        updateView();
      } else {
        view.appendMessage("Your pet is not tired at all!");
      }
    } catch (IllegalStateException e) {
      view.appendMessage(e.getMessage());
    }
  }

  /**
   * Handle the creation of a new pet.
   */
  private void handleNewPet() {
    if (pet.getHealth() <= 0) {
      if (pet.getTimeManager() != null) {
        // restart the time manager
        pet.getTimeManager().restart();
      }
      view.enableActionButtons();
      view.updateRestButton(false); // set the rest button text to "Rest"
      view.updatePetIcon("normal");
      updateView();
      view.appendMessage("Created a new pet!");
    }
  }

  /**
   * Update the view based on the pet's health, current state, and pet icon.
   */
  public void updateView() {
    view.updateHealth(pet.getHealth());
    view.updateState(pet.getDisplayState());
    // special case: if the pet's health is 0, set the pet icon to dead.png
    if (pet.getHealth() <= 0) {
      view.updatePetIcon("dead.png");
      return;
    }
    // Update the pet icon based on the current state
    view.updatePetIcon(pet.getCurrentStateObject().getStateIcon());
    // Update the rest button based on the sleep state,
    // if isSleeping is true, set the text to "Wake Up"
    view.updateRestButton(pet.isSleeping());
    // iterate through the concurrent hashmap and update the state score
    for (PetState state : PetState.values()) {
      if (state != PetState.NORMAL) {
        // each state in <enum> PetState has a score except NORMAL , update the state score
        view.updateStateScore(state, pet.getStateScore(state));
      }
    }
  }
}