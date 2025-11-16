package com.example;
import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
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

    NtfyConnectionImpl(){
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName){
        this.hostName = hostName;
    }

    @Override
    public boolean send(String message) {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/adam"))
                .build();
        try {
            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return true;
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
            System.out.println("Interrupted sending message");
        }
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        String startId = "8TuugOLkvDz1"; // just to make the app wont load 10000000 messages
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/adam/json?since=3hPbr2dcIUiU"))
                .build();


        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())

                .thenAccept(response -> response.body()
                        .peek(s -> System.out.println(s))
                        .map(s -> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (Exception e) {
                                System.out.println("JSON parse fail: " + s);
                                return null;
                            }
                        })
                        .filter(msg -> msg != null)
                        .filter(msg -> "message".equals(msg.event()))
                        .forEach(messageHandler)
                );
    }

    @Override
    public boolean sendFile(File file){
        try {
            String mime = java.nio.file.Files.probeContentType(file.toPath());

            if(mime == null) mime =  "application/octet-stream";

            long size = file.length();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/adam"))
                    .header("Filename", file.getName())
                    .header("Content-Type", mime)
                    .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .build();

            http.sendAsync(request, HttpResponse.BodyHandlers.discarding());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
