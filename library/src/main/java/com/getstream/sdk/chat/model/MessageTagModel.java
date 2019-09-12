package com.getstream.sdk.chat.model;

public class MessageTagModel {
    public String type;
    public int position;

    public MessageTagModel(String type, int position) {
        this.type = type;
        this.position = position;
    }
}
