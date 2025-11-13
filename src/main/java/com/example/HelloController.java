package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;


public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;

    @FXML
    public ImageView ntfyIcon;

    @FXML
    private TextField inputField;

    @FXML
    private Label messageLabel;

    @FXML
    private File selectedFile;

    /**
     * Initializes the controller's UI: sets the ntfy icon, applies the model greeting, binds the message list, and configures how messages are displayed.
     *
     * <p>Each list item is shown in the format "[topic] message".</p>
     */
    @FXML
    private void initialize() {

        ntfyIcon.setImage(new Image("Ntfy-Icon.png"));

        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }

        messageView.setItems(model.getMessages());

        messageView.setCellFactory(lv -> new ListCell<NtfyMessageDto>() {

            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    String displayText = String.format("[%s] %s", item.topic(), item.message());
                    setText(displayText);

                }
            }
        });

    }

    /**
     * Opens a file chooser dialog, stores the chosen file in {@code selectedFile}, and updates {@code messageLabel} to reflect whether a file was selected.
     *
     * The dialog is titled "Välj en fil att bifoga" and is shown using the window from {@code messageLabel}'s scene as the owner.
     * If a file is selected, {@code messageLabel} is set to "Fil vald: <filename>"; otherwise it is set to "Ingen fil vald.".
     */
    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en fil att bifoga");

        Stage stage = (Stage) messageLabel.getScene().getWindow();

        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            messageLabel.setText("Fil vald: " + selectedFile.getName());
        } else {
            messageLabel.setText("Ingen fil vald.");
        }
    }

    /**
     * Send either the currently selected file or the text entered in the input field.
     *
     * <p>If a file is selected, attempts to send that file and updates {@code messageLabel} to indicate
     * success or failure; on success the selected file reference is cleared. If no file is selected,
     * trims the input text and, if non-empty, sends the trimmed text and clears the input field.
     * If the trimmed text is empty, logs "Meddelandet är tomt" to standard output and sets
     * {@code messageLabel} to prompt the user for input.
     */
    public void sendMessage() {

        if (selectedFile != null) {

            boolean success = model.sendFile(selectedFile);

            if (success) {
                messageLabel.setText("Filen '" + selectedFile.getName() + "' skickad!");
                selectedFile = null;
            } else {
                messageLabel.setText("Fel vid sändning av fil.");
            }
            return;
        }


        String enteredText = inputField.getText().trim();

        if (enteredText.trim().isEmpty()) {
            System.out.println("Meddelandet är tomt");
            messageLabel.setText("Vänligen skriv ett meddelande.");
            return;
        }
        enteredText = enteredText.trim();
        model.sendMessage(enteredText);

        inputField.clear();
    }


}