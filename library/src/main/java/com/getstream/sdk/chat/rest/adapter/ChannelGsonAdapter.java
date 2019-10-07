package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Config;
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

public class ChannelGsonAdapter extends TypeAdapter<Channel> {
    @Override
    public void write(JsonWriter writer, Channel channel) throws IOException {
        HashMap<String, Object> data = new HashMap<>();

        if (channel.getExtraData() != null && !channel.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : channel.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        if (channel.getId() != null)
            data.put("id", channel.getId());

        if (channel.getCid() != null)
            data.put("cid", channel.getCid());

        if (channel.getType() != null)
            data.put("type", channel.getType());

        if (channel.getDeletedAt() != null)
            data.put("deleted_at", channel.getDeletedAt());

        TypeAdapter adapter = GsonConverter.Gson().getAdapter(HashMap.class);
        adapter.write(writer, data);
    }

    @Override
    public Channel read(JsonReader reader) throws IOException {
        Gson gson = GsonConverter.Gson();

        TypeAdapter adapter = gson.getAdapter(HashMap.class);
        HashMap<String, Object> value = (HashMap) adapter.read(reader);

        if (value == null) {
            return null;
        }

        Channel channel = new Channel();
        HashMap<String, Object> extraData = new HashMap<>();

        // TODO: is approach (like Java) is super dumb, we decode data twice
        for (HashMap.Entry<String, Object> set : value.entrySet()) {
            String json = gson.toJson(set.getValue());
            // Set Reserved Data
            switch (set.getKey()) {
                case "id":
                    channel.setId((String) set.getValue());
                    continue;
                case "cid":
                    channel.setCid((String) set.getValue());
                    continue;
                case "type":
                    channel.setType((String) set.getValue());
                    continue;
                case "created_by":
                    channel.setCreatedByUser(gson.fromJson(json, User.class));
                    continue;
                case "last_message_at":
                    channel.setLastMessageDate(gson.fromJson(json, Date.class));
                    continue;
                case "created_at":
                    channel.setCreatedAt(gson.fromJson(json, Date.class));
                    continue;
                case "deleted_at":
                    channel.setDeletedAt(gson.fromJson(json, Date.class));
                    continue;
                case "updated_at":
                    channel.setUpdatedAt(gson.fromJson(json, Date.class));
                    continue;
                case "frozen":
                    channel.setFrozen((boolean) set.getValue());
                    continue;
                case "config":
                    channel.setConfig(new Gson().fromJson(json, Config.class));
                    continue;
                case "member_count":
                    continue;
            }
            // Set Extra Data       
            extraData.put(set.getKey(), set.getValue());
        }
        channel.setExtraData(extraData);
        return channel;
    }
}
