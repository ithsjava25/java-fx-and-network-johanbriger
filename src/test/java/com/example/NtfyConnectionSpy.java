package com.example;

import java.io.File;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

   String message;

    /**
     * Records the outgoing message in the spy for inspection and indicates the message was accepted.
     *
     * @param message the message to send (and store for later inspection)
     * @return `true` if the message was accepted for sending, `false` otherwise
     */
    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    /**
     * No-op implementation that accepts a message handler but does not invoke it.
     *
     * @param messageHandler the handler for incoming messages; ignored by this spy
     */
    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {

    }

    /**
     * Attempt to send the given file over this connection. This spy implementation performs no operation.
     *
     * @param file the file to send
     * @return `true` if the file was sent successfully, `false` otherwise; this implementation always returns `false`
     */
    @Override
    public boolean sendFile(File file) {
        return false;
    }
}