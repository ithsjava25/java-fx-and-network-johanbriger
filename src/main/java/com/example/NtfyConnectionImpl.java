package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean send(String message) {

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
               .uri(URI.create(hostName + "/mytopic")).build();
       try {
           var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
           return true;
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
           System.out.println("Interrupted while sending message");
        }
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {

        HttpRequest httpRequest =  HttpRequest.newBuilder()
                .GET().uri(URI.create(hostName + "/mytopic/json")).build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s ->  mapper.readValue(s, NtfyMessageDto.class))
                        .peek(System.out::println)
                        .filter(message->message.event().equals("message"))
                        .forEach(messageHandler));
    }

    @Override
    public boolean sendFile(File file) {
        if (file == null || !file.exists()) {
            System.out.println("Filen är ogiltig eller saknas.");
            return false;
        }

        try {

            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream"; // Standard om typen inte hittas
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/mytopic"))
                    .header("Content-Type", contentType)
                    .header("Filename", file.getName())
                    .POST(BodyPublishers.ofByteArray(fileBytes))
                    .build();

            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            } else {
                System.out.println("Fel vid sändning av fil. Statuskod: " + response.statusCode());
                return false;
            }

        } catch (IOException e) {
            System.out.println("Error reading or sending file: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Interrupted while sending file");
            Thread.currentThread().interrupt();
        }
        return false;
    }

}
