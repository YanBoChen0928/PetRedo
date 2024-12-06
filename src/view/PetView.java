package view;

import model.Pet;
import model.PetAction;
import model.PetState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Graphical user interface for the Virtual Pet application.
 * Displays pet status and provides interaction controls.
 */
public class PetView extends JFrame {
    // UI Components
    private JLabel healthLabel;
    private JLabel stateLabel;
    private JTextArea messageArea;
    private JButton feedButton;
    private JButton cleanButton;
    private JButton playButton;
    private JButton restButton;
    private JButton newPetButton;
    private JLabel petIconLabel;
    
    /**
     * Creates and initializes the main application window.
     */
    public PetView() {
        initializeUI();
    }
    
    /**
     * Sets up the user interface layout and components.
     * Organizes components into status panel, message area, button panel, and pet icon.
     */
    private void initializeUI() {
        setTitle("Virtual Pet");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(400, 600);
        
        // Status panel setup
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        healthLabel = new JLabel("Health: 100");
        stateLabel = new JLabel("State: Normal");
        statusPanel.add(healthLabel);
        statusPanel.add(stateLabel);
        
        // Message area setup
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setPreferredSize(new Dimension(380, 100));
        
        // Action buttons setup
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        feedButton = new JButton("Feed");
        cleanButton = new JButton("Clean");
        playButton = new JButton("Play");
        restButton = new JButton("Rest");
        buttonPanel.add(feedButton);
        buttonPanel.add(cleanButton);
        buttonPanel.add(playButton);
        buttonPanel.add(restButton);
        
        // Pet icon setup
        petIconLabel = new JLabel();
        petIconLabel.setHorizontalAlignment(JLabel.CENTER);
        updatePetIcon("normal.png");
        
        // New Pet button setup
        newPetButton = new JButton("New Pet");
        newPetButton.setEnabled(false);
        
        // Layout assembly
        add(statusPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(petIconLabel, BorderLayout.CENTER);
        add(newPetButton, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
    }
    
    /**
     * Updates the displayed health value and handles death state.
     * @param health Current health value
     */
    public void updateHealth(int health) {
        healthLabel.setText("Health: " + health);
        if (health <= 0) {
            newPetButton.setEnabled(true);
            disableActionButtons();
        }
    }
    
    /**
     * Updates the displayed state text.
     * @param state Current state name
     */
    public void updateState(String state) {
        stateLabel.setText("State: " + state);
    }
    
    /**
     * Updates the pet's icon based on current state.
     * @param iconPath Path to the icon resource
     */
    public void updatePetIcon(String iconPath) {
        ImageIcon icon = new ImageIcon(getClass().getResource("/pet_normal.png"));
        petIconLabel.setIcon(icon);
    }
    
    /**
     * Adds a message to the message area.
     * @param message Message to display
     */
    public void appendMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }
    
    // Action button listener setters
    public void setFeedButtonListener(ActionListener listener) {
        feedButton.addActionListener(listener);
    }
    
    public void setCleanButtonListener(ActionListener listener) {
        cleanButton.addActionListener(listener);
    }
    
    public void setPlayButtonListener(ActionListener listener) {
        playButton.addActionListener(listener);
    }
    
    public void setRestButtonListener(ActionListener listener) {
        restButton.addActionListener(listener);
    }
    
    public void setNewPetButtonListener(ActionListener listener) {
        newPetButton.addActionListener(listener);
    }
    
    /**
     * Updates the Rest button text based on sleep state.
     * @param isSleeping Current sleep state
     */
    public void updateRestButton(boolean isSleeping) {
        restButton.setText(isSleeping ? "Wake Up" : "Rest");
    }
    
    /**
     * Disables all action buttons when pet dies.
     */
    private void disableActionButtons() {
        feedButton.setEnabled(false);
        cleanButton.setEnabled(false);
        playButton.setEnabled(false);
        restButton.setEnabled(false);
    }
    
    /**
     * Enables all action buttons and disables new pet button.
     * Called when creating a new pet.
     */
    public void enableActionButtons() {
        feedButton.setEnabled(true);
        cleanButton.setEnabled(true);
        playButton.setEnabled(true);
        restButton.setEnabled(true);
        newPetButton.setEnabled(false);
    }
} 