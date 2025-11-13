package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(
        String id,
        long time,
        long expires,
        String event,
        String topic,
        String message,
        Attachment attachment
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attachment(
            String name,
            String type,
            String url,
            long expires,
            long size
    ){}

}
