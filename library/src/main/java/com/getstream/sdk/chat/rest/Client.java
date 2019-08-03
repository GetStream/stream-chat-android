package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QueryOptions;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.rest.response.GetChannelsResponse;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;

public class Client {


    public String getApiKey() {
        return ApiKey;
    }

    private String ApiKey;
    protected String UserToken;
    protected String BaseURL;
    protected User UserData;
    protected String ConnectionID;

    public Client(String ApiKey){
        this.ApiKey = ApiKey;

    }

    public void setUser(User user, String token){}

    public void disconnect(){}

    public void setAnonymousUser(){}

    public void setGuestUser(){}

    public void on(){}

    public void off(){}

    public void sendFile(){}

    public void queryUsers(){}



    public void addDevice(){}

    public void getDevices(){}

    public void removeDevice(){}

//    public Channel channel(){
//        return new Channel();
//    }

    public void muteUser(){}

    public void unmuteUser(){}

    public void flagMessage(){}

    public void unflagMessage(){}

    public void markAllRead(){}

    public void updateMessage(){}

    public void deleteMessage(){}

    public void getMessage(){}

}
