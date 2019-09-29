package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.model.Attachment;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AttachmentGsonAdapter extends TypeAdapter<Attachment> {
    @Override
    public void write(JsonWriter out, Attachment attachment) throws IOException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("type", attachment.getType());
        data.put("name", attachment.getName());
        data.put("image", attachment.getImage());
        if (attachment.getExtraData() != null && !attachment.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : attachment.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        adapter.write(out, data);
    }

    @Override
    public Attachment read(JsonReader in) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        HashMap<String, Object> value = (HashMap<String, Object>) adapter.read(in);
        HashMap<String, Object> data = new HashMap<>();
        for (Map.Entry<String, Object> set : value.entrySet()) {
            if (set.getKey().equals("id"))
                continue;
            data.put(set.getKey(), set.getValue());
        }

        Attachment attachment = new Attachment();
        attachment.setExtraData(data);
        return attachment;
    }
}
