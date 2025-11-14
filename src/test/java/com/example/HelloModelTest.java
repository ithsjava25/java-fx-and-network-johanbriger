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
    @DisplayName("When calling sendMessage it should send to the fake server")
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
    void receiveMessageFromFakeServer(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

        String host = "http://localhost:" + wmRuntimeInfo.getHttpPort();
        var com = new NtfyConnectionImpl(host);

        CompletableFuture<NtfyMessageDto> receivedMessageFuture = new CompletableFuture<>();

        String responseBody = "{\"id\":\"testId123\",\"time\":1762501214,\"event\":\"message\",\"topic\":\"mytopic\",\"message\":\"Testmeddelande\"}\n";

        stubFor(get("/mytopic/json")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseBody)
                        .withHeader("Content-Type", "application/json")));

        com.receive(receivedMessageFuture::complete);

        NtfyMessageDto receivedDto = receivedMessageFuture.get(5, java.util.concurrent.TimeUnit.SECONDS);

        verify(getRequestedFor(urlEqualTo("/mytopic/json")));

        assertThat(receivedDto).isNotNull();
        assertThat(receivedDto.event()).isEqualTo("message");
        assertThat(receivedDto.topic()).isEqualTo("mytopic");
        assertThat(receivedDto.message()).isEqualTo("Testmeddelande");
    }

}