package com.getstream.sdk.chat.view;


import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;

public interface StreamView {
    void setClient(Client client);
    void setChannel(Channel client);
}