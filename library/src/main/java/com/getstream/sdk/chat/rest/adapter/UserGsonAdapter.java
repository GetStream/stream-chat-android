package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.model.Device;
import com.getstream.sdk.chat.model.Mute;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserGsonAdapter extends TypeAdapter<User> {
    @Override
    public void write(JsonWriter writer, User user) throws IOException {

        HashMap<String, Object> data = new HashMap<>();

        if (user.getExtraData() != null && !user.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : user.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        if (user.getId() != null)
            data.put("id", user.getId());

        if (user.getName() != null)
            data.put("name", user.getName());

        if (user.getImage() != null)
            data.put("image", user.getImage());

        TypeAdapter adapter = GsonConverter.Gson().getAdapter(HashMap.class);
        adapter.write(writer, data);
    }

    @Override
    public User read(JsonReader reader) throws IOException {
        Gson gson = GsonConverter.Gson();

        TypeAdapter adapter = gson.getAdapter(HashMap.class);
        HashMap<String, Object> value =  (HashMap) adapter.read(reader);

        if (value == null) {
            return null;
        }

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
                case "banned":
                    user.setBanned(gson.fromJson(json, Boolean.class));
                    continue;
                case "total_unread_count":
                    user.setTotalUnreadCount(gson.fromJson(json, Integer.class));
                    continue;
                case "unread_channels":
                    user.setUnreadChannels(gson.fromJson(json, Integer.class));
                    continue;
                case "invisible":
                    user.setInvisible(gson.fromJson(json, Boolean.class));
                    continue;
                case "devices":
                    user.setDevices(gson.fromJson(json, new TypeToken<ArrayList<Device>>(){}.getType()));
                    continue;
                case "mutes":
                    user.setMutes(gson.fromJson(json, new TypeToken<ArrayList<Mute>>(){}.getType()));
                    continue;
                case "unread_count":
                    gson.fromJson(json, Integer.class);
                    continue;
            }
            // Set Extra Data
            extraData.put(set.getKey(), set.getValue());
        }

        if (user.getName() == null) user.setName("");
        user.setExtraData(extraData);
        return user;
    }
}
