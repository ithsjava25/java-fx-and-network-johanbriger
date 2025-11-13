package com.example;

import java.io.File;
import java.util.function.Consumer;

public interface NtfyConnection {

    /**
 * Send a text message through this connection.
 *
 * @param message the message payload to send
 * @return `true` if the message was sent successfully, `false` otherwise
 */
boolean send(String message);

    /**
 * Registers a handler to be invoked for each incoming NtfyMessageDto.
 *
 * @param messageHandler the consumer to be called with each received message
 */
void receive(Consumer<NtfyMessageDto> messageHandler);

    /**
 * Sends the given file over this connection.
 *
 * @param file the file to send
 * @return `true` if the file was sent successfully, `false` otherwise
 */
boolean sendFile(File file);
}