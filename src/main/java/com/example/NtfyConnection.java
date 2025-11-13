package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {
    public boolean send(String message);
    boolean sendFile(java.io.File file);
    public void receive(Consumer<NtfyMessageDto> messageHandler);
}
