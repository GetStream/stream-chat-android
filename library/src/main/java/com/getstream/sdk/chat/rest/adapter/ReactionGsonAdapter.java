package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReactionGsonAdapter extends TypeAdapter<Reaction> {
    @Override
    public void write(JsonWriter writer, Reaction reaction) throws IOException {
        HashMap<String, Object> data = new HashMap<>();

        if (reaction.getExtraData() != null && !reaction.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : reaction.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        if (reaction.getCreatedAt() != null) {
            data.put("created_at", reaction.getCreatedAt());
        }

        if (reaction.getMessageId() != null) {
            data.put("message_id", reaction.getMessageId());
        }

        if (reaction.getType() != null) {
            data.put("type", reaction.getType());
        }

        if (reaction.getUser() != null) {
            data.put("user", reaction.getUser());
        }

        if (reaction.getUserID() != null) {
            data.put("user_id", reaction.getUserID());
        }

        if (reaction.getScore() != null) {
            data.put("score", reaction.getScore());
        }

        TypeAdapter adapter = GsonConverter.Gson().getAdapter(HashMap.class);
        adapter.write(writer, data);
    }

    @Override
    public Reaction read(JsonReader reader) throws IOException {
        Gson gson = GsonConverter.Gson();

        TypeAdapter adapter = gson.getAdapter(HashMap.class);
        Map<String, Object> value = (HashMap) adapter.read(reader);

        if (value == null) {
            return null;
        }

        Reaction reaction = new Reaction();
        HashMap<String, Object> extraData = new HashMap<>();

        for (HashMap.Entry<String, Object> set : value.entrySet()) {
            String json = gson.toJson(set.getValue());
            // Set Reserved Data
            switch (set.getKey()) {
                case "user":
                    reaction.setUser(gson.fromJson(json, User.class));
                    continue;
                case "user_id":
                    reaction.setUserID((String) set.getValue());
                    continue;
                case "type":
                    reaction.setType((String) set.getValue());
                    continue;
                case "message_id":
                    reaction.setMessageId((String) set.getValue());
                    continue;
                case "created_at":
                    reaction.setCreatedAt(gson.fromJson(json, Date.class));
                    continue;
                case "score":
                    int score = ((Number) set.getValue()).intValue();
                    reaction.setScore(score);
                    continue;
            }
            // Set Extra Data
            extraData.put(set.getKey(), set.getValue());
        }
        return reaction;
    }
}
