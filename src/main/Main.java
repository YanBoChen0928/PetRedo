package main;

import model.Pet;
import model.TimeManager;
import view.PetView;
import controller.PetController;
import javax.swing.SwingUtilities;

/**
 * Main entry point for the Virtual Pet application.
 * Initializes and connects all components of the MVC architecture.
 */
public class Main {
    /**
     * Application entry point.
     * Creates and initializes the Model, View, and Controller components.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Initialize MVC components
        Pet pet = new Pet();
        TimeManager timeManager = new TimeManager(pet);
        PetView view = new PetView();
        PetController controller = new PetController(pet, view);
        
        // Set up update listener
        timeManager.setUpdateListener(() -> {
            SwingUtilities.invokeLater(() -> {
                controller.updateView();
            });
        });
        
        // Start GUI on Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            view.setVisible(true);
        });
        
        // Register shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timeManager.shutdown();
        }));
    }
} 