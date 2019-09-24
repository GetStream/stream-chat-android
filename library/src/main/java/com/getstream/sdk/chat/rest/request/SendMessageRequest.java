package com.getstream.sdk.chat.rest.request;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendMessageRequest {
    @SerializedName("message")
    @Expose
    Map<String, Object> message;

    public SendMessageRequest(String id, String text, List<Attachment> attachments, String parentId,
                              boolean showInChannel, List<String> mentionedUserIDs) {
        this(text, attachments, parentId, showInChannel, mentionedUserIDs);
        message.put("id", id);
    }

    public SendMessageRequest(String text,
                              List<Attachment> attachments,
                              String parentId,
                              boolean showInChannel,
                              List<String> mentionedUserIDs) {
        message = new HashMap<>();
        message.put("text", text);

        if (parentId != null) {
            message.put("parent_id", parentId);
            message.put("show_in_channel", showInChannel);
        }
        if (attachments != null && !attachments.isEmpty()) {
            List<Map> attachmentMaps = new ArrayList<>();
            boolean isGiphy = false;
            for (Attachment attachment_ : attachments) {
                Map attachment;
                String json = GsonConverter.Gson().toJson(attachment_);
                attachment = GsonConverter.Gson().fromJson(json, Map.class);
                attachment.remove("config");
                if (attachment_.getType().equals(ModelType.attach_image)) {
                    attachment.remove("asset_url");
                    attachment.remove("file_size");
                }
                attachmentMaps.add(attachment);
                if (!isGiphy && attachment_.getType().equals(ModelType.attach_giphy))
                    isGiphy = true;
            }
            message.put("attachments", attachmentMaps);
            if (isGiphy){
                message.put("command", ModelType.attach_giphy);
                Map<String, String> commandInfo = new HashMap<>();
                commandInfo.put("name","Giphy");
                message.put("command_info", commandInfo);
            }
        }

        if (mentionedUserIDs != null && !mentionedUserIDs.isEmpty())
            message.put("mentioned_users", mentionedUserIDs);

    }
}
