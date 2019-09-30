package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.rest.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserGsonAdapter extends TypeAdapter<User> {
    @Override
    public void write(JsonWriter writer, User user) throws IOException {

        HashMap<String, Object> data = new HashMap<>();

        if (user.getId() != null)
            data.put("id", user.getId());

        if (user.getName() != null)
            data.put("name", user.getName());

        if (user.getImage() != null)
            data.put("image", user.getImage());

        if (user.getRole() != null)
            data.put("role", user.getRole());

        if (user.getCreatedAt() != null)
            data.put("created_at", user.getCreatedAt());

        if (user.getUpdatedAt() != null)
            data.put("updated_at", user.getUpdatedAt());

        if (user.getLastActive() != null)
            data.put("last_active", user.getLastActive());

        if (user.getOnline() != null)
            data.put("online", user.getOnline());

        data.put("total_unread_count", user.getTotalUnreadCount());
        data.put("unread_channels", user.getUnreadChannels());

        // Set Extra Data
        if (user.getExtraData() != null && !user.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : user.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        adapter.write(writer, data);
    }

    @Override
    public User read(JsonReader reader) throws IOException {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

        TypeAdapter adapter = gson.getAdapter(HashMap.class);
        HashMap<String, Object> value =  (HashMap) adapter.read(reader);

        User user = new User();
        HashMap<String, Object> extraData = new HashMap<>();

        for (HashMap.Entry<String, Object> set : value.entrySet()) {            
            String json = gson.toJson(set.getValue());     
            // Set Reserved Data
            switch (set.getKey()) {
                case "id":
                    user.setId((String) set.getValue());
                    continue;
                case "name":
                    user.setName((String) set.getValue());
                    continue;
                case "image":
                    user.setImage((String) set.getValue());
                    continue;
                case "role":
                    user.setRole((String) set.getValue());
                    continue;
                case "created_at":
                    user.setCreatedAt(gson.fromJson(json, Date.class));
                    continue;
                case "updated_at":
                    user.setUpdatedAt(gson.fromJson(json, Date.class));
                    continue;
                case "last_active":
                    user.setLastActive(gson.fromJson(json, Date.class));
                    continue;
                case "online":
                    user.setOnline(gson.fromJson(json, Boolean.class));
                    continue;
                case "total_unread_count":
                    user.setTotalUnreadCount(gson.fromJson(json, Integer.class));
                    continue;
                case "unread_channels":
                    user.setUnreadChannels(gson.fromJson(json, Integer.class));
                    continue;
            }
            // Set Extra Data
            extraData.put(set.getKey(), set.getValue());
        }
        user.setExtraData(extraData);
        return user;
    }
}
