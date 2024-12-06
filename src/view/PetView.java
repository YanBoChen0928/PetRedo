package view;

import java.net.URL;
import model.Pet;
import model.PetAction;
import model.PetState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    
    // Pet display components
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private static final String[] STATES = {
        "normal", "hungry", "dirty", "tired", "bored", "sleeping", "happy"
    };
    private final Map<String, JLabel> stateLabels = new HashMap<>();
    
    // 添加新的UI元素
    private Map<PetState, JLabel> scoreLabels = new HashMap<>();
    
    /**
     * Creates and initializes the main application window.
     */
    public PetView() {
        initializeUI();
        initializeStateImages();
    }
    
    /**
     * Sets up the user interface layout and components.
     */
    private void initializeUI() {
        setTitle("Virtual Pet");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        
        // 左側面板：狀態和分數
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        
        // 狀態面板
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        healthLabel = new JLabel("Health: 100");
        stateLabel = new JLabel("State: Normal");
        statusPanel.add(healthLabel);
        statusPanel.add(stateLabel);
        leftPanel.add(statusPanel, BorderLayout.NORTH);
        
        // 分數面板
        JPanel scorePanel = new JPanel(new GridLayout(4, 1, 5, 5));
        scorePanel.setBorder(BorderFactory.createTitledBorder("State Scores"));
        for (PetState state : PetState.values()) {
            if (state != PetState.NORMAL) {
                JLabel scoreLabel = new JLabel(state.toString() + " Score: 0");
                scoreLabels.put(state, scoreLabel);
                scorePanel.add(scoreLabel);
            }
        }
        leftPanel.add(scorePanel, BorderLayout.CENTER);
        
        // 中央面板：寵物圖示和消息
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        
        // 寵物圖示
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setPreferredSize(new Dimension(200, 200));
        centerPanel.add(cardPanel, BorderLayout.CENTER);
        
        // 消息區域
        messageArea = new JTextArea(5, 30);
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        centerPanel.add(scrollPane, BorderLayout.SOUTH);
        
        // 右側面板：操作按鈕
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        feedButton = new JButton("Feed");
        cleanButton = new JButton("Clean");
        playButton = new JButton("Play");
        restButton = new JButton("Rest");
        newPetButton = new JButton("New Pet");
        newPetButton.setEnabled(false);
        
        buttonPanel.add(feedButton);
        buttonPanel.add(cleanButton);
        buttonPanel.add(playButton);
        buttonPanel.add(restButton);
        buttonPanel.add(newPetButton);
        
        // 組裝主界面
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
        
        // 設置窗口位置
        setLocationRelativeTo(null);
    }
    
    /**
     * Initializes image labels for all pet states.
     */
    private void initializeStateImages() {
        for (String state : STATES) {
            JLabel imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            
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
     * Adds a message to the message area.
     * @param message Message to display
     */
    public void appendMessage(String message) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        messageArea.append(String.format("[%s] %s%n", timestamp, message));
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
    
    /**
     * Updates the state score display.
     * @param state The state to update
     * @param score The score to display
     */
    public void updateStateScore(PetState state, int score) {
        JLabel label = scoreLabels.get(state);
        if (label != null) {
            String stateText = state.toString().substring(0, 1).toUpperCase() + 
                             state.toString().substring(1).toLowerCase();
            String scoreBar = "|".repeat(score) + "-".repeat(10 - score);
            label.setText(String.format("%s: [%s] (%d/10)", stateText, scoreBar, score));
        }
    }
} 