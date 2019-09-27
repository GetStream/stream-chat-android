package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.model.Channel;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChannelGsonAdapter extends TypeAdapter<Channel> {
    @Override
    public void write(JsonWriter out, Channel channel) throws IOException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", channel.getId());
        data.put("type", channel.getType());
        data.put("name", channel.getName());
        data.put("image", channel.getImage());
        if (channel.getExtraData() != null && !channel.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : channel.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        adapter.write(out, data);
    }

    @Override
    public Channel read(JsonReader in) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(Channel.class);
        Channel value = (Channel) adapter.read(in);
        return value;
    }
}
