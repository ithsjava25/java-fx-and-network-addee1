package com.example;

import java.io.File;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;
    File sentFile;
    Consumer<NtfyMessageDto> handler;

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    public boolean sendFile(File file){
        this.sentFile = file;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.handler = messageHandler;
    }

    public void simulateIncoming(NtfyMessageDto message){
        if (handler !=null){
            handler.accept(message);
        }
    }
}
