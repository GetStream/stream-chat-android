package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.response.MessageResponse;

import retrofit2.Call;

public class Channel {

    private String type;
    private String id;
    private Client client;

    public Channel(String type, String id, Client client){
        this.type = type;
        this.id = id;
        this.client = client;
//        retrofitClient = this.client.getRetrofitServiceFactory().create(ChannelRetrofit.class);
    }

//    public Call<MessageResponse> sendMessage(Message message){
//        SendMessageRequest request = new SendMessageRequest("", null, null, true, null);
//        return retrofitClient.sendMessage(type, id, client.getApiKey(), request);
//    }
}
