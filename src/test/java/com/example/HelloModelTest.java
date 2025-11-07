package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CompletableFuture;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @Test
    @DisplayName("When calling sendMessage it should call connection send")
    void sendMessageCallsConnectionWithMessageToSend(){

        //Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act
        model.sendMessage("Hello World");
        //Assert
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo){
        var com = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(com);
        model.setMessageToSend("Hello World");
        stubFor(post("/mytopic").willReturn(ok()));
        model.sendMessage("Hello World");
        // Verify send to server
        verify(postRequestedFor(WireMock.urlEqualTo("/mytopic"))
                .withRequestBody(containing("Hello World")));
    }

    @Test
    @DisplayName("When calling receive, it should process messages from the server")
    void receiveMessagesFromFakeServer(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

        // Arrange
        // 1. Skapa anslutningen mot den falska servern
        String host = "http://localhost:" + wmRuntimeInfo.getHttpPort();
        var com = new NtfyConnectionImpl(host);

        // 2. Skapa en Future för att fånga det mottagna meddelandet asynkront
        CompletableFuture<NtfyMessageDto> receivedMessageFuture = new CompletableFuture<>();

        // Den JSON som WireMock ska returnera. (Anpassa ID och TIME vid behov)
        // OBS: Ntfy skickar normalt JSON per rad i receive-läget
        String responseBody = "{\"id\":\"testId123\",\"time\":1762501214,\"event\":\"message\",\"topic\":\"mytopic\",\"message\":\"Testmeddelande\"}\n";

        // 3. Stubba servern (GET /mytopic/json) för att returnera testdata
        stubFor(get("/mytopic/json")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)
                        .withHeader("Content-Type", "application/json")));

        // 4. Implementera messageHandler (Consumer<NtfyMessageDto>)
        //    som ska fånga meddelandet och slutföra Future:n.
        com.receive(receivedMessageFuture::complete);

        // Act
        // Vänta på att det asynkrona nätverksanropet ska slutföras (max 5 sekunder)
        NtfyMessageDto receivedDto = receivedMessageFuture.get(5, java.util.concurrent.TimeUnit.SECONDS);

        // Assert
        // Verifiera att servern anropades
        verify(getRequestedFor(urlEqualTo("/mytopic/json")));

        // Verifiera innehållet i det mottagna objektet
        assertThat(receivedDto).isNotNull();
        assertThat(receivedDto.event()).isEqualTo("message");
        assertThat(receivedDto.topic()).isEqualTo("mytopic");
        assertThat(receivedDto.message()).isEqualTo("Testmeddelande");
    }

}