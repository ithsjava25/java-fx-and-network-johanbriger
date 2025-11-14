package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.awt.*;
import java.io.File;
import java.net.URI;


public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    @FXML
    public ListView<NtfyMessageDto> messageView;

    @FXML
    public ImageView ntfyIcon;

    @FXML
    private TextField inputField;

    @FXML
    private Label messageLabel;


    private File selectedFile;

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
                    setGraphic(null);
                } else {
                    String displayText = String.format("[%s] %s", item.topic(), item.message());
                    setText(displayText);

                    if (item.attachmentUrl() != null && !item.attachmentUrl().isEmpty()) {
                        Hyperlink downloadLink = new Hyperlink("Ladda ner fil");
                        downloadLink.setOnAction(e -> {
                            try {
                                Desktop.getDesktop().browse(new URI(item.attachmentUrl()));
                            } catch (Exception ex) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error Opening Link");
                                alert.setContentText("Could not open attachment: " + ex.getMessage());
                                alert.showAndWait();
                            }
                        });
                        setGraphic(downloadLink);
                    }else  {
                        setGraphic(null);
                    }


                }
            }
        });
    }

    

    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en fil att bifoga");

        var scene = messageLabel.getScene();
        if (scene == null || scene.getWindow() == null) {
            messageLabel.setText("Fel: Fönster ej tillgängligt.");
            return;
        }
        Stage stage = (Stage) scene.getWindow();

        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            messageLabel.setText("Fil vald: " + selectedFile.getName());
        } else {
            messageLabel.setText("Ingen fil vald.");
        }
    }

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

        model.sendMessage(enteredText);

        inputField.clear();
    }


}
