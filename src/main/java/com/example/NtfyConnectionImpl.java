package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;
import java.nio.file.Path;
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
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            } else {
                System.out.println("Error sending message. Status code: " + response.statusCode());
                return false;
            }
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
           System.out.println("Interrupted while sending message");
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {

        HttpRequest httpRequest =  HttpRequest.newBuilder()
                .GET().uri(URI.create(hostName + "/mytopic/json")).build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s ->  {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (Exception e) {
                                // Logga felet som inträffade vid JSON-parsning
                                System.err.println("JSON parsing error: " + e.getMessage());
                                return null; // Returnera null vid parsningsfel
                            }
                        })
                        .filter(java.util.Objects::nonNull)
                        .peek(System.out::println)
                        .filter(message->message.event().equals("message"))
                        .forEach(messageHandler));
    }

    @Override
    public boolean sendFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            System.out.println("Filen är ogiltig eller saknas.");
            return false;
        }

        Path filePath = file.toPath();

        try {


            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/mytopic"))
                    .header("Content-Type", contentType)
                    .header("Filename", file.getName())
                    .POST(BodyPublishers.ofFile(filePath))
                    .build();

            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("Filen skickades");
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
