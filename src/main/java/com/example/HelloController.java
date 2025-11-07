package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private TextField inputField;

    @FXML
    private Label messageLabel;

    @FXML
    private File selectedFile;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }


        messageView.setItems(model.getMessages());

        messageView.setCellFactory(lv -> new ListCell<NtfyMessageDto>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    // Rensa cellen om den är tom
                    setText(null);
                } else {
                    String displayText = String.format("[%s] %s", item.topic(), item.message());
                    setText(displayText);

                    // Valfritt: Lägg till t.ex. tidpunkten för meddelandet
                    // setText(item.message() + " (" + item.time() + ")");
                }
            }
        });

    }

    @FXML
    private void handleAttachFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en fil att bifoga");

        // Öppna dialogrutan. showOpenDialog behöver en referens till fönstret (Stage).
        // Vi kan använda vilket GUI-element som helst för att få referensen till fönstret:
        Stage stage = (Stage) messageLabel.getScene().getWindow();

        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Uppdatera en label eller ditt inputField för att visa att en fil valts
            messageLabel.setText("Fil vald: " + selectedFile.getName());
        } else {
            messageLabel.setText("Ingen fil vald.");
        }
    }

    public void sendMessage() {

        if (selectedFile != null) {
            // Skicka FILEN om en fil har valts
            boolean success = model.sendFile(selectedFile); // Antag att du har en sendFile i HelloModel

            if (success) {
                messageLabel.setText("Filen '" + selectedFile.getName() + "' skickad!");
                selectedFile = null; // Återställ
            } else {
                messageLabel.setText("FEL vid sändning av fil.");
            }
            return; // Avsluta eftersom vi skickade filen istället för text
        }


        String enteredText = inputField.getText().trim();

        if (enteredText.trim().isEmpty()) {
            System.out.println("Meddelandet är null eller tomt/bara mellanslag. Avbryter sändning.");
            messageLabel.setText("Vänligen skriv ett meddelande.");
            return;
        }
        enteredText = enteredText.trim();
        model.sendMessage(enteredText);

        inputField.clear();
    }


}
