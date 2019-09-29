package com.getstream.sdk.chat.rest.adapter;

import android.util.Log;

import com.getstream.sdk.chat.rest.Message;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessageGsonAdapter extends TypeAdapter<Message> {
    @Override
    public void write(JsonWriter out, Message message) throws IOException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", message.getId());
        data.put("type", message.getType());

        if (message.getExtraData() != null && !message.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : message.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        adapter.write(out, data);
    }

    @Override
    public Message read(JsonReader in) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        HashMap<String, Object> value = (HashMap<String, Object>) adapter.read(in);
        HashMap<String, Object> data = new HashMap<>();
        for (Map.Entry<String, Object> set : value.entrySet()) {
            if (set.getKey().equals("id"))
                continue;
            data.put(set.getKey(), set.getValue());
        }

        Message message = new Message();
        message.setExtraData(data);
        return message;
    }
}
