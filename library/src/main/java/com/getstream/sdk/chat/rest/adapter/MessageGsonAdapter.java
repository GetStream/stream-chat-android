package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.storage.Sync;
import com.google.gson.Gson;
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
        TypeAdapter adapter = GsonConverter.Gson().getAdapter(HashMap.class);
        /*
         * When serializing Message if the message object is null,
         * this TypeAdapter should not be called, however currently called this function.
         * Added null check to prevent NPE if null.
         *
         * This issue was before Gson 2.4 but currently we are using the latest version.
         *      https://stackoverflow.com/a/55884142/798165
         */
        if (message == null){
            adapter.write(writer, data);
            return;
        }

        if (message.getExtraData() != null && !message.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : message.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        if (message.getId() != null)
            data.put("id", message.getId());

        if (message.getText() != null)
            data.put("text", message.getText());

        if (message.getAttachments() != null)
            data.put("attachments", message.getAttachments());

        if (message.getMentionedUsersId() != null)
            data.put("mentioned_users", message.getMentionedUsersId());

        if (message.getParentId() != null)
            data.put("parent_id", message.getParentId());

        if (message.getAttachments() != null
                && !message.getAttachments().isEmpty()) {
            boolean isGiphy = false;
            for (Attachment attachment : message.getAttachments()) {
                if (!isGiphy && attachment.getType().equals(ModelType.attach_giphy))
                    isGiphy = true;
            }
            if (isGiphy){
                data.put("command", ModelType.attach_giphy);
                Map<String, String> commandInfo = new HashMap<>();
                commandInfo.put("name","Giphy");
                data.put("command_info", commandInfo);
            }
        }
        adapter.write(writer, data);
    }

    @Override
    public Message read(JsonReader reader) throws IOException {
        Gson gson = GsonConverter.Gson();

        TypeAdapter adapter = gson.getAdapter(HashMap.class);
        Map<String, Object> value = (HashMap) adapter.read(reader);

        if (value == null) {
            return null;
        }

        Message message = new Message();
        message.setSyncStatus(Sync.SYNCED);
        HashMap<String, Object> extraData = new HashMap<>();

        // TODO: is approach (like Java) is super dumb, we decode data twice
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
                case "channel":
                    message.setChannel(gson.fromJson(json, Channel.class));
                    continue;
                case "attachments":
                    message.setAttachments(gson.fromJson(json, new TypeToken<List<Attachment>>(){}.getType()));
                    continue;
                case "latest_reactions":
                    message.setLatestReactions(gson.fromJson(json, new TypeToken<List<Reaction>>(){}.getType()));
                    continue;
                case "own_reactions":
                    message.setOwnReactions(gson.fromJson(json, new TypeToken<List<Reaction>>(){}.getType()));
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
                    message.setMentionedUsers(gson.fromJson(json, new TypeToken<List<User>>(){}.getType()));
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
