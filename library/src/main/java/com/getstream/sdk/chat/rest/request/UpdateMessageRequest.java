package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.model.ModelType;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateMessageRequest {
    @SerializedName("message")
    @Expose
    Map<String, Object> message;

    public UpdateMessageRequest(Message message, List<Attachment> attachments, List<String> mentionedUserIDs) {
        Gson gson = new Gson();
        String json = gson.toJson(message);
        Map<String, Object> map = new HashMap<>();
        this.message = (Map<String, Object>) gson.fromJson(json, map.getClass());

        if (attachments != null && !attachments.isEmpty()) {
            Gson gson1 = new Gson();
            List<Map> attachmentMaps = new ArrayList<>();
            for (Attachment attachment_ : attachments) {
                Map<String, Object> attachment;
                String json1 = gson1.toJson(attachment_);
                attachment = (Map<String, Object>) gson1.fromJson(json1, Map.class);
                attachment.remove("config");
                attachmentMaps.add(attachment);
                if (attachment_.getType().equals(ModelType.attach_image)) {
                    attachment.remove("asset_url");
                    attachment.remove("file_size");
                }
            }
            this.message.put("attachments", attachmentMaps);
        }

        if (mentionedUserIDs != null && !mentionedUserIDs.isEmpty())
            this.message.put("mentioned_users", mentionedUserIDs);


        this.message.remove("id");
        this.message.remove("latest_reactions");
        this.message.remove("own_reactions");
        this.message.remove("reaction_counts");
        this.message.remove("reply_count");
        this.message.remove("type");
        this.message.remove("user");
        this.message.remove("created_at");
        this.message.remove("updated_at");
        this.message.remove("html");
        this.message.remove("command");
        // Custom Keys
        this.message.remove("extraData");
        this.message.remove("isStartDay");
        this.message.remove("isYesterday");
        this.message.remove("isToday");
        this.message.remove("date");
        this.message.remove("time");
        this.message.remove("isIncoming");
        this.message.remove("isDelivered");
        this.message.remove("convertDateToString");
        this.message.remove("isCommandMessage");
        this.message.remove("differentTime");
        this.message.remove("TimeElapsed");
        this.message.remove("equals");
    }
}
