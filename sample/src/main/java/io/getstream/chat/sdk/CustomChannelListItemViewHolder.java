package io.getstream.chat.sdk;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getstream.sdk.chat.adapter.BaseChannelListItemViewHolder;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelResponse;

public class CustomChannelListItemViewHolder extends BaseChannelListItemViewHolder {
    public TextView tv_initials, tv_last_message;
    public CustomChannelListItemViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);
        // init views
        tv_initials = itemView.findViewById(com.getstream.sdk.chat.R.id.tv_initials);
        tv_last_message = itemView.findViewById(com.getstream.sdk.chat.R.id.tv_last_message);
    }

    @Override
    public void bind(Context context, ChannelResponse channelResponse,
                     int position, View.OnClickListener clickListener,
                     View.OnLongClickListener longClickListener) {
        // bind data
        Channel channel = channelResponse.getChannel();
        tv_initials.setText(channel.getInitials());
        Message lastMessage = channelResponse.getLastMessage();
        if (TextUtils.isEmpty(lastMessage.getText())) {
            if (!lastMessage.getAttachments().isEmpty()) {
                Attachment attachment = lastMessage.getAttachments().get(0);
                tv_last_message.setText(!TextUtils.isEmpty(attachment.getTitle()) ? attachment.getTitle() : attachment.getFallback());
            } else {
                tv_last_message.setText("");
            }
        } else {
           tv_last_message.setText(lastMessage.getText());
        }
    }
}
