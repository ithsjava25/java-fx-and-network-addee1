package com.example;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @BeforeAll
    static void initJavaFx() {
        Platform.startup(() -> {});
    }

    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called ")
    void sendMessageCallsConnectionWithMessageToSend() {

        // Arrange Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");

        // Act When
        model.sendMessage();

        // Assert Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        stubFor(post("/adam").willReturn(ok()));

        model.sendMessage();

        // Verify call made to server
        verify(postRequestedFor(urlEqualTo("/adam"))
                .withRequestBody(matching("Hello World")));
    }

    @Test
    @DisplayName("Given file when calling sendFile then connection.sendFile should be called")
    void sendFileCallConnectionSendFile() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        File file = new File("dummy.txt");
        model.sendFile(file);

        assertThat(spy.sentFile).isEqualTo(file);
    }

    @Test
    @DisplayName("sendFile should upload file to the server using PUT")
    void sendFileToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);

        File temp = File.createTempFile("upload_test", ".txt");

        stubFor(put("/adam").willReturn(ok()));

        model.sendFile(temp);

        // Wait a moment for async request to complete
        Thread.sleep(500);

        verify(putRequestedFor(urlEqualTo("/adam")));

    }

    @Test
    @DisplayName("Model should receive messages when connection invokes handler")
    void receiveMessageShouldAddToModelViewHander() throws Exception{
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        NtfyMessageDto incoming = new NtfyMessageDto(
                "123",
                1000,
                0,
                "message",
                "adam",
                "this is a test hehehe",
                null
        );

        spy.simulateIncoming(incoming);

        // Wait a moment for async request to complete
        Thread.sleep(500);

        assertThat(model.getMessages()).containsExactly(incoming);
    }

    @Test
    @DisplayName("Model constructor should register receive handler on connection")
    void constructorShouldRegisterHandler() {

        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        assertThat(spy.handler).isNotNull();
    }

    @Test
    @DisplayName("messageToSendProperty should update when setting message")
    void messagePropertyShouldUpdate() {

        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        model.setMessageToSend("Hello!");

        assertThat(model.getMessageToSend()).isEqualTo("Hello!");
    }

    @Test
    @DisplayName("getGreeting should return the expected greeting text")
    void greetingShouldBeCorrect() {

        var model = new HelloModel(new NtfyConnectionSpy());

        assertThat(model.getGreeting()).isEqualTo("Chat Client by Adam");
    }




}