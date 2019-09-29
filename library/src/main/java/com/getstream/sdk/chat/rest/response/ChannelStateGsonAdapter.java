package com.getstream.sdk.chat.rest.response;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Collections;

public class ChannelStateGsonAdapter extends TypeAdapter<ChannelState> {
    @Override
    public void write(JsonWriter out, ChannelState value) throws IOException {
        throw new IOException("not supported");
    }

    @Override
    public ChannelState read(JsonReader in) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(ChannelState.class);
        ChannelState value = (ChannelState) adapter.read(in);
        value.getChannel().setChannelState(value);
        if (value.getReads() != null && !value.getReads().isEmpty()){
            Collections.sort(value.getReads(), (ChannelUserRead o1, ChannelUserRead o2) -> o1.getLastRead().compareTo(o2.getLastRead()));
        }
        return value;
    }
}
