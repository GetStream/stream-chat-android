package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.rest.User;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserGsonAdapter extends TypeAdapter<User> {
    @Override
    public void write(JsonWriter out, User user) throws IOException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("image", user.getImage());
        if (user.getExtraData() != null && !user.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : user.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        adapter.write(out, data);
    }

    @Override
    public User read(JsonReader in) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(User.class);
        User value = (User) adapter.read(in);
        return value;
    }
}
