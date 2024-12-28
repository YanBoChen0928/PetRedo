package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import model.PetState;

/**
 * Graphical user interface for the Virtual Pet application.
 * Displays pet status and provides interaction controls.
 */
public class PetView extends JFrame {
  private static final String[] STATES = {
      "normal", "hungry", "dirty", "tired", "bored", "sleeping", "happy", "dead"
  };
  private final Map<String, JLabel> stateLabels = new HashMap<>();
  // UI Components
  private JLabel healthLabel;
  private JLabel stateLabel;
  private JTextArea messageArea;
  private JButton feedButton;
  private JButton cleanButton;
  private JButton playButton;
  private JButton restButton;
  private JButton newPetButton;
  // Pet display components
  private JPanel cardPanel;
  private CardLayout cardLayout;
  // State score display components
  private Map<PetState, JLabel> scoreLabels = new HashMap<>();

  /**
   * Creates and initializes the main application window.
   */
  public PetView() {
    initializeUserInterface();
    initializeStateImages();
  }

  /**
   * Sets up the user interface layout and components.
   */
  private void initializeUserInterface() {
    setTitle("Virtual Pet");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));
    setSize(800, 400);


    // Health and state panel
    JPanel statusPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    healthLabel = new JLabel("Health: 100");
    stateLabel = new JLabel("State: Normal");
    statusPanel.add(healthLabel);
    statusPanel.add(stateLabel);

    // Score panel
    JPanel scorePanel = new JPanel(new GridLayout(4, 1, 5, 5));
    scorePanel.setBorder(BorderFactory.createTitledBorder("State Scores"));
    for (PetState state : PetState.values()) {
      if (state != PetState.NORMAL) {
        JLabel scoreLabel = new JLabel(state.toString() + " Score: 0");
        scoreLabels.put(state, scoreLabel);
        scorePanel.add(scoreLabel);
      }
    }

    // left Panel: health, state, and score
    JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
    leftPanel.add(statusPanel, BorderLayout.NORTH);
    leftPanel.add(scorePanel, BorderLayout.CENTER);

    // Pet display panel
    cardLayout = new CardLayout();
    cardPanel = new JPanel(cardLayout);
    cardPanel.setPreferredSize(new Dimension(200, 200));

    // center Panel: pet display and message area
    JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

    centerPanel.add(cardPanel, BorderLayout.CENTER);

    // Message area
    messageArea = new JTextArea(5, 30);
    messageArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(messageArea);
    centerPanel.add(scrollPane, BorderLayout.SOUTH);

    // right Panel: action buttons
    JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    feedButton = new JButton("Feed");
    cleanButton = new JButton("Clean");
    restButton = new JButton("Rest");
    playButton = new JButton("Play");
    newPetButton = new JButton("New Pet");
    newPetButton.setEnabled(false);

    buttonPanel.add(newPetButton);
    buttonPanel.add(feedButton);
    buttonPanel.add(cleanButton);
    buttonPanel.add(restButton);
    buttonPanel.add(playButton);

    // Add panels to the main window
    add(leftPanel, BorderLayout.WEST);
    add(centerPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.EAST);

    // Center the window on the screen
    setLocationRelativeTo(null);
  }

  /**
   * Initializes image labels for all pet states.
   * !!! important: images must be in the "images" folder in the resources directory !!!
   * using getClassLoader().getResource() to load images from the resources directory.
   */
  private void initializeStateImages() {
    for (String state : STATES) {
      JLabel imageLabel = new JLabel();
      imageLabel.setHorizontalAlignment(JLabel.CENTER);
      // to load images from the resources directory, and put them in the cardPanel,
      // reference: 162: updatePetIcon() method
      // use getClass().getClassLoader().getResource() to load images from the resources directory
      // we use getClassLoader() because use classPath instead of file path for .jar and .dmg use
      String imagePath = "images/" + state + ".png";
      URL resource = getClass().getClassLoader().getResource(imagePath);
      if (resource != null) {
        ImageIcon icon = new ImageIcon(resource);
        Image image = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(image));
      } else {
        System.err.println("Warning: Could not load image: " + imagePath);
        imageLabel.setText("Image not found: " + state);
      }

      stateLabels.put(state, imageLabel);
      cardPanel.add(imageLabel, state);
    }
  }

  /**
   * Updates the pet's displayed state.
   *
   * @param state The state to display
   */
  public void updatePetIcon(String state) {
    String stateName = state.toLowerCase().replace(".png", "");
    if (stateLabels.containsKey(stateName)) {
      cardLayout.show(cardPanel, stateName);
    } else {
      System.err.println("Warning: Unknown state: " + state);
    }
  }

  /**
   * Updates the displayed health value and handles death state.
   *
   * @param health Current health value
   */
  // update the health value and handle the death state (the pet is dead)
  public void updateHealth(int health) {
    healthLabel.setText("Health: " + health);
    if (health <= 0) {
      updatePetIcon("dead");
      newPetButton.setEnabled(true);
      disableActionButtons();
    }
  }

  /**
   * Updates the displayed state text.
   *
   * @param state Current state name
   */
  public void updateState(String state) {
    stateLabel.setText("State: " + state);
  }

  /**
   * Adds a message to the message area.
   *
   * @param message Message to display
   */
  public void appendMessage(String message) {
    LocalDateTime now = LocalDateTime.now();
    String timestamp = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    messageArea.append(String.format("[%s] %s%n", timestamp, message));
    messageArea.setCaretPosition(messageArea.getDocument().getLength());
  }

  /**
   * Series of Listener setters for the action buttons.
   *
   * @param listener Listener to set
   */

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

  /**
   * Sets the listener for the New Pet button.
   *
   * @param listener The listener to set
   */
  public void setNewPetButtonListener(ActionListener listener) {
    // remove existing listeners
    for (ActionListener al : newPetButton.getActionListeners()) {
      newPetButton.removeActionListener(al);
    }
    // add new listener
    newPetButton.addActionListener(listener);
  }

  /**
   * Updates the Rest button text based on sleep state.
   *
   * @param isSleeping Current sleep state
   */
  // update the Rest button text based on the sleep state
  public void updateRestButton(boolean isSleeping) {
    restButton.setText(isSleeping ? "Wake Up" : "Rest");
  }

  /**
   * Disables all action buttons when pet dies.
   */
  // disable all action buttons when the pet dies (the pet is dead)
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

  /**
   * Updates the state score display.
   *
   * @param state The state to update
   * @param score The score to display
   */

  // bar display for each state score
  public void updateStateScore(PetState state, int score) {
    JLabel label = scoreLabels.get(state);
    if (label != null) {
      String stateText = state.toString().substring(0, 1).toUpperCase()
          + state.toString().substring(1).toLowerCase();
      String scoreBar = "|".repeat(score) + "-".repeat(10 - score);
      label.setText(String.format("%s: [%s] (%d/10)", stateText, scoreBar, score));
    }
  }
} 