package com.getstream.sdk.chat.rest.apimodel.request;

import android.util.Log;

import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.utils.Global;
import com.google.gson.Gson;
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

    public SendMessageRequest(ChannelResponse response, String text, List<Attachment> attachments, String parentId, boolean showInChannel) {
        message = new HashMap<>();
        message.put("text", text);
        if (parentId != null) {
            message.put("parent_id", parentId);
            message.put("show_in_channel", showInChannel);
        }
        if (attachments != null && !attachments.isEmpty()) {
            Gson gson = new Gson();
            List<Map> attachmentMaps = new ArrayList<>();
            for (Attachment attachment_ : attachments) {
                Map<String, Object> attachment;
                String json = gson.toJson(attachment_);
                attachment = (Map<String, Object>) gson.fromJson(json, Map.class);
                attachment.remove("config");
                attachmentMaps.add(attachment);
                if (attachment_.getType().equals(ModelType.attach_image)) {
                    attachment.remove("asset_url");
                    attachment.remove("file_size");
                }

                Log.d("SendMessageRequest", "attachmentMaps: " + attachmentMaps);
            }
            message.put("attachments", attachmentMaps);
        }
        if (Global.getMentionedUserIDs(response, text) != null) {
            List<String> mentionedUserIDs = Global.getMentionedUserIDs(response, text);
            if (!mentionedUserIDs.isEmpty())
                message.put("mentioned_users", mentionedUserIDs);
        }
    }
}
