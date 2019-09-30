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
        HashMap<String, Object> value = (HashMap) adapter.read(reader);

        User user = new User();
        HashMap<String, Object> extraData = new HashMap<>();

        for (HashMap.Entry<String, Object> set : value.entrySet()) {
            boolean isReserved = false;
            String json = gson.toJson(set.getValue());
            Date date;
            // Set Reserved Data
            switch (set.getKey()) {
                case "id":
                    isReserved = true;
                    user.setId((String) set.getValue());
                    break;
                case "name":
                    isReserved = true;
                    user.setName((String) set.getValue());
                    break;
                case "image":
                    isReserved = true;
                    user.setImage((String) set.getValue());
                    break;
                case "role":
                    isReserved = true;
                    user.setRole((String) set.getValue());
                    break;
                case "created_at":
                    isReserved = true;
                    date = gson.fromJson(json, Date.class);
                    user.setCreatedAt(date);
                    break;
                case "updated_at":
                    isReserved = true;
                    date = gson.fromJson(json, Date.class);
                    user.setUpdatedAt(date);
                    break;
                case "last_active":
                    isReserved = true;
                    date = gson.fromJson(json, Date.class);
                    user.setLastActive(date);
                    break;
                case "online":
                    isReserved = true;
                    user.setOnline(gson.fromJson(json, Boolean.class));
                    break;
                case "total_unread_count":
                    isReserved = true;
                    user.setTotalUnreadCount(gson.fromJson(json, Integer.class));
                    break;
                case "unread_channels":
                    isReserved = true;
                    user.setUnreadChannels(gson.fromJson(json, Integer.class));
                    break;
            }
            // Set Extra Data
            if (!isReserved)
                extraData.put(set.getKey(), set.getValue());
        }
        user.setExtraData(extraData);
        return user;
    }
}
