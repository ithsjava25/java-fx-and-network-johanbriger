package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;


public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();


    /**
     * Creates a HelloModel bound to the given NtfyConnection and begins listening for incoming messages.
     *
     * @param connection the NtfyConnection used to send and receive messages
     */
    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }

    /**
     * Provides the live observable list of chat messages maintained by the model.
     *
     * @return the live ObservableList of NtfyMessageDto containing the current messages
     */
    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /**
     * Update the model's messageToSend property with the provided text.
     *
     * @param message the text to set as the message to send
     */
    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }


    /**
     * Provide the fixed greeting displayed in the application's UI.
     *
     * @return the greeting text "Welcome to NTFY chat"
     */
    public String getGreeting() {
        return "Welcome to NTFY chat";
    }

    /**
     * Send the given text message through the configured NTFY connection.
     *
     * @param enteredText the message text to send
     */
    public void sendMessage(String enteredText) {
        connection.send(enteredText);
    }

    /**
     * Begins listening for incoming messages and appends each received message to the model's messages list.
     *
     * Received messages are added on the JavaFX Application Thread so UI bindings can be safely updated.
     */
    public void receiveMessage() {
        connection.receive(m->Platform.runLater(()->messages.add(m)));
    }

    /**
     * Sends the given file to the configured NTFY connection.
     *
     * @param file the file to send
     * @return `true` if the file was sent successfully, `false` otherwise
     */
    public boolean sendFile(File file) {
        return connection.sendFile(file);
    }



}