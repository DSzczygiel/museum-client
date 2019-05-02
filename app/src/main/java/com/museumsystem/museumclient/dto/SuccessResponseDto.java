package com.museumsystem.museumclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class SuccessResponseDto {
    @JsonProperty("data")
    HashMap<String, String> messages;

    public HashMap<String, String> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, String> messages) {
        this.messages = messages;
    }
}
