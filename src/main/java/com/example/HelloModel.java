package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.Objects;


public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();


    public HelloModel(NtfyConnection connection) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }


    public String getGreeting() {
        return "Welcome to NTFY chat";
    }

    public void sendMessage(String enteredText) {
        connection.send(enteredText);
    }

    public void receiveMessage() {
        connection.receive(m->Platform.runLater(()->messages.add(m)));
    }

    public boolean sendFile(File file) {
        return connection.sendFile(file);
    }



}
