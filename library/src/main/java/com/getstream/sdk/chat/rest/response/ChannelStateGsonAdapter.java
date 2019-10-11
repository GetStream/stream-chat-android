package com.getstream.sdk.chat.rest.response;

import android.text.TextUtils;

import com.getstream.sdk.chat.rest.Message;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelStateGsonAdapter extends TypeAdapter<ChannelState> {
    @Override
    public void write(JsonWriter out, ChannelState value) throws IOException {
        throw new IOException("not supported");
    }

    @Override
    public ChannelState read(JsonReader in) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(ChannelState.class);
        ChannelState channelState = (ChannelState) adapter.read(in);

        /*Sort Reads by read date*/
        if (channelState.getReads() != null && !channelState.getReads().isEmpty())
            Collections.sort(channelState.getReads(), (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLastRead().compareTo(o2.getLastRead()));

        /*Filter wrong Thread messages from main channel message list*/
        if (channelState.getMessages() != null && !channelState.getMessages().isEmpty()) {
            List<Message> newMessages = new ArrayList<>(channelState.getMessages());
            for (Message message : newMessages)
                if (!TextUtils.isEmpty(message.getParentId()))
                    channelState.getMessages().remove(message);
        }

        /*Set ChannelState to Channel*/
        channelState.getChannel().setChannelState(channelState);

        return channelState;
    }
}
