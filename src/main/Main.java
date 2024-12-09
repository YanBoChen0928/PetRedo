package main;

import controller.PetController;
import javax.swing.SwingUtilities;
import model.Pet;
import model.TimeManager;
import view.PetView;

/**
 * Main entry point for the Virtual Pet application.
 * Initializes and connects all components of the MVC architecture.
 */
//ToDo: optimize in the future, should implement an interface as the only instance to
//      interact with the model, view, and controller

public class Main {
  /**
   * Application entry point.
   * Creates and initializes the Model, View, and Controller components.
   *
   * @param args Command line arguments (not used)
   */
  public static void main(String[] args) {
    // Initialize MVC components
    Pet pet = new Pet();
    TimeManager timeManager = new TimeManager(pet);
    pet.setTimeManager(timeManager);
    PetView view = new PetView();
    PetController controller = new PetController(pet, view);

    // Set up update listener
    //!!! very important step to avoid the time messed up
    timeManager.setUpdateListener(() -> {
      SwingUtilities.invokeLater(() -> {
        controller.updateView();
      });
    });

    // Start GUI on Event Dispatch Thread
    javax.swing.SwingUtilities.invokeLater(() -> {
      view.setVisible(true);
    });

    // Register shutdown hook for cleanup, when the application is closed
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      timeManager.shutdown();
    }));
  }
} 