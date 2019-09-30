package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageGsonAdapter extends TypeAdapter<Message> {
    @Override
    public void write(JsonWriter writer, Message message) throws IOException {
        HashMap<String, Object> data = new HashMap<>();
        if (message.getId() != null)
            data.put("id", message.getId());

        if (message.getText() != null)
            data.put("text", message.getText());

        if (message.getHtml() != null)
            data.put("html", message.getHtml());

        if (message.getType() != null)
            data.put("type", message.getType());

        if (message.getUser() != null)
            data.put("user", message.getUser());

        if (message.getAttachments() != null)
            data.put("attachments", message.getAttachments());

        if (message.getLatestReactions() != null)
            data.put("latest_reactions", message.getLatestReactions());

        if (message.getOwnReactions() != null)
            data.put("own_reactions", message.getOwnReactions());

        data.put("reply_count", message.getReplyCount());

        if (message.getCreatedAt() != null)
            data.put("created_at", message.getCreatedAt());

        if (message.getUpdatedAt() != null)
            data.put("updated_at", message.getUpdatedAt());

        if (message.getDeletedAt() != null)
            data.put("deleted_at", message.getDeletedAt());

        if (message.getMentionedUsers() != null)
            data.put("mentioned_users", message.getMentionedUsers());

        if (message.getReactionCounts() != null)
            data.put("reaction_counts", message.getReactionCounts());

        if (message.getParentId() != null)
            data.put("parent_id", message.getParentId());

        if (message.getCommand() != null)
            data.put("command", message.getCommand());

        if (message.getCommandInfo() != null)
            data.put("command_info", message.getCommandInfo());
        // Set Extra Data
        if (message.getExtraData() != null && !message.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : message.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        adapter.write(writer, data);
    }

    @Override
    public Message read(JsonReader reader) throws IOException {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

        TypeAdapter adapter = gson.getAdapter(HashMap.class);
        HashMap<String, Object> value = (HashMap) adapter.read(reader);

        Message message = new Message();
        HashMap<String, Object> extraData = new HashMap<>();

        for (HashMap.Entry<String, Object> set : value.entrySet()) {
            boolean isReserved = false;
            String json = gson.toJson(set.getValue());
            Date date;
            // Set Reserved Data
            switch (set.getKey()) {
                case "id":
                    isReserved = true;
                    message.setId((String) set.getValue());
                    break;
                case "text":
                    isReserved = true;
                    message.setText((String) set.getValue());
                    break;
                case "html":
                    isReserved = true;
                    message.setHtml((String) set.getValue());
                    break;
                case "type":
                    isReserved = true;
                    message.setType((String) set.getValue());
                    break;
                case "user":
                    isReserved = true;
                    message.setUser(gson.fromJson(json, User.class));
                    break;
                case "attachments":
                    isReserved = true;
                    message.setAttachments(gson.fromJson(json, List.class));
                    break;
                case "latest_reactions":
                    isReserved = true;
                    message.setLatestReactions(gson.fromJson(json, List.class));
                    break;
                case "own_reactions":
                    isReserved = true;
                    message.setOwnReactions(gson.fromJson(json, List.class));
                    break;
                case "reply_count":
                    isReserved = true;
                    message.setReplyCount(gson.fromJson(json, Integer.class));
                    break;
                case "created_at":
                    isReserved = true;
                    message.setCreatedAt(gson.fromJson(json, Date.class));
                    break;
                case "updated_at":
                    isReserved = true;
                    message.setUpdatedAt(gson.fromJson(json, Date.class));
                    break;
                case "deleted_at":
                    isReserved = true;
                    message.setDeletedAt(gson.fromJson(json, Date.class));
                    break;
                case "mentioned_users":
                    isReserved = true;
                    message.setMentionedUsers(gson.fromJson(json, List.class));
                    break;
                case "reaction_counts":
                    isReserved = true;
                    message.setReactionCounts(gson.fromJson(json, Map.class));
                    break;
                case "parent_id":
                    isReserved = true;
                    message.setParentId((String) set.getValue());
                    break;
                case "command":
                    isReserved = true;
                    message.setCommand((String) set.getValue());
                    break;
                case "command_info":
                    isReserved = true;
                    message.setCommandInfo(gson.fromJson(json, Map.class));
                    break;
            }
            // Set Extra Data
            if (!isReserved)
                extraData.put(set.getKey(), set.getValue());
        }
        message.setExtraData(extraData);
        return message;
    }
}
