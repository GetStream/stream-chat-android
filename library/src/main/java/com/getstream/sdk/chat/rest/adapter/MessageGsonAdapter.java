package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageGsonAdapter extends TypeAdapter<Message> {
    private static final String TAG = MessageGsonAdapter.class.getSimpleName();

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
        Map<String, Object> value = (HashMap) adapter.read(reader);

        Message message = new Message();
        HashMap<String, Object> extraData = new HashMap<>();

        for (HashMap.Entry<String, Object> set : value.entrySet()) {
            String json = gson.toJson(set.getValue());
            // Set Reserved Data
            switch (set.getKey()) {
                case "id":
                    message.setId((String) set.getValue());
                    continue;
                case "text":
                    message.setText((String) set.getValue());
                    continue;
                case "html":
                    message.setHtml((String) set.getValue());
                    continue;
                case "type":
                    message.setType((String) set.getValue());
                    continue;
                case "user":
                    message.setUser(gson.fromJson(json, User.class));
                    continue;
                case "attachments":
                    List<Attachment>attachments = gson.fromJson(json, new TypeToken<List<Attachment>>(){}.getType());
                    message.setAttachments(attachments);
                    continue;
                case "latest_reactions":
                    List<Reaction>reactions = gson.fromJson(json, new TypeToken<List<Reaction>>(){}.getType());
                    message.setLatestReactions(reactions);
                    continue;
                case "own_reactions":
                    List<Reaction>ownReactions = gson.fromJson(json, new TypeToken<List<Reaction>>(){}.getType());
                    message.setOwnReactions(ownReactions);
                    continue;
                case "reply_count":
                    message.setReplyCount(gson.fromJson(json, Integer.class));
                    continue;
                case "created_at":
                    message.setCreatedAt(gson.fromJson(json, Date.class));
                    continue;
                case "updated_at":
                    message.setUpdatedAt(gson.fromJson(json, Date.class));
                    continue;
                case "deleted_at":
                    message.setDeletedAt(gson.fromJson(json, Date.class));
                    continue;
                case "mentioned_users":
                    List<User>users = gson.fromJson(json, new TypeToken<List<User>>(){}.getType());
                    message.setMentionedUsers(users);
                    continue;
                case "reaction_counts":
                    message.setReactionCounts(gson.fromJson(json, new TypeToken<Map<String, Integer>>(){}.getType()));
                    continue;
                case "parent_id":
                    message.setParentId((String) set.getValue());
                    continue;
                case "command":
                    message.setCommand((String) set.getValue());
                    continue;
                case "command_info":
                    message.setCommandInfo(gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType()));
                    continue;
            }
            // Set Extra Data
            extraData.put(set.getKey(), set.getValue());
        }
        message.setExtraData(extraData);
        return message;
    }
}
