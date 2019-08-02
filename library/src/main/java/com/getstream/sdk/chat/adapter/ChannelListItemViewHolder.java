package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.ReadIndicator;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import ru.noties.markwon.image.ImagesPlugin;

public class ChannelListItemViewHolder extends BaseChannelListItemViewHolder {

    public ConstraintLayout cl_root;
    public TextView tv_initials, tv_name, tv_last_message, tv_date, tv_indicator_initials, tv_click, tv_unread;
    public ImageView iv_avatar, iv_indicator;

    private Context context;

    private Markwon markwon;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    
    public ChannelListItemViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);

        cl_root = itemView.findViewById(R.id.cl_root);
        tv_initials = itemView.findViewById(R.id.tv_initials);
        tv_name = itemView.findViewById(R.id.tv_name);
        tv_last_message = itemView.findViewById(R.id.tv_last_message);
        tv_date = itemView.findViewById(R.id.tv_date);
        tv_indicator_initials = itemView.findViewById(R.id.tv_indicator_initials);
        tv_click = itemView.findViewById(R.id.tv_click);
        iv_avatar = itemView.findViewById(R.id.iv_avatar);
        iv_indicator = itemView.findViewById(R.id.iv_indicator);
        tv_unread = itemView.findViewById(R.id.tv_unread);
    }

    @Override
    public void bind(Context context, ChannelResponse channelResponse, int position,
                     View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {

        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        if (markwon == null)
            this.markwon = Markwon.builder(context)
                    .usePlugin(ImagesPlugin.create(context))
                    .usePlugin(CorePlugin.create())
                    .usePlugin(StrikethroughPlugin.create())
                    .build();

        Message lastMessage = channelResponse.getLastMessage();
        configUIs(position);
        configChannelInfo(channelResponse);
        configIndicatorUserInfo(channelResponse);
        if (lastMessage != null) {
            configMessageDate(lastMessage);
            configLastMessageDate(channelResponse, lastMessage);
        } else {
            tv_last_message.setText("");
            tv_date.setText("");
        }

        tv_click.setOnClickListener(view -> {
            Utils.setButtonDelayEnable(view);
            view.setTag(channelResponse.getChannel().getId());

            tv_click.setBackgroundColor(Color.parseColor("#14000000"));
            new Handler().postDelayed(() ->tv_click.setBackgroundColor(0), 500);
            this.clickListener.onClick(view);
        });

        tv_click.setOnLongClickListener(view -> {
            view.setTag(channelResponse.getChannel().getId());
            this.longClickListener.onLongClick(view);
            return true;
        });
    }

    private void configUIs(int position) {
        tv_click.setBackgroundColor(0);
        if (position == 0)
            cl_root.setBackgroundResource(R.drawable.round_channel_list_new);
        else
            cl_root.setBackgroundResource(0);
    }

    private void configChannelInfo(ChannelResponse channelResponse) {
        Channel channel = channelResponse.getChannel();
        if (!TextUtils.isEmpty(channel.getName())) {
            tv_initials.setText(channel.getInitials());
            tv_name.setText(channel.getName());
            Utils.circleImageLoad(iv_avatar, channel.getImage());
            if (StringUtility.isValidImageUrl(channel.getImage())) {
                iv_avatar.setVisibility(View.VISIBLE);
                tv_initials.setVisibility(View.INVISIBLE);
            } else {
                iv_avatar.setVisibility(View.INVISIBLE);
                tv_initials.setVisibility(View.VISIBLE);
            }
        } else {
            User opponent = Global.getOpponentUser(channelResponse);
            if (opponent != null) {
                tv_initials.setText(opponent.getUserInitials());
                Utils.circleImageLoad(iv_avatar, opponent.getImage());
                tv_name.setText(opponent.getName());
                tv_initials.setVisibility(View.VISIBLE);
                iv_avatar.setVisibility(View.VISIBLE);
            } else {
                tv_initials.setVisibility(View.GONE);
                iv_avatar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void configIndicatorUserInfo(ChannelResponse channelResponse) {
        if (!Global.component.channel.isShowReadIndicator()) {
            tv_indicator_initials.setVisibility(View.INVISIBLE);
            iv_indicator.setVisibility(View.INVISIBLE);
            return;
        }
        if (Global.component.channel.getReadIndicator() == ReadIndicator.LAST_READ_USER) {
            iv_indicator.setVisibility(View.VISIBLE);
            tv_indicator_initials.setVisibility(View.VISIBLE);
            tv_unread.setVisibility(View.GONE);

            User lastReadUser = channelResponse.getLastReadUser();
            Message lastMessage = channelResponse.getLastMessage();
            if (lastMessage == null) {
                tv_indicator_initials.setVisibility(View.INVISIBLE);
                iv_indicator.setVisibility(View.INVISIBLE);
            } else if (lastReadUser != null) {
                if (StringUtility.isValidImageUrl(lastReadUser.getImage())) {
                    Utils.circleImageLoad(iv_indicator, lastReadUser.getImage());
                    iv_indicator.setVisibility(View.VISIBLE);
                    tv_indicator_initials.setVisibility(View.INVISIBLE);
                } else {
                    tv_indicator_initials.setText(lastReadUser.getUserInitials());
                    tv_indicator_initials.setVisibility(View.VISIBLE);
                    iv_indicator.setVisibility(View.INVISIBLE);
                }
            } else {
                tv_indicator_initials.setVisibility(View.INVISIBLE);
                iv_indicator.setVisibility(View.INVISIBLE);
            }
        } else {
            iv_indicator.setVisibility(View.GONE);
            tv_indicator_initials.setVisibility(View.GONE);
            tv_unread.setVisibility(View.VISIBLE);
            int unreadMessageCount = channelResponse.getUnreadMessageCount();
            if (unreadMessageCount == 0) {
                tv_unread.setVisibility(View.GONE);
                return;
            }
            tv_unread.setText(unreadMessageCount + " unread");
        }

    }

    private void configMessageDate(Message lastMessage) {
        if (TextUtils.isEmpty(lastMessage.getText())) {
            if (!lastMessage.getAttachments().isEmpty()) {
                Attachment attachment = lastMessage.getAttachments().get(0);
                tv_last_message.setText(!TextUtils.isEmpty(attachment.getTitle()) ? attachment.getTitle() : attachment.getFallback());
            } else {
                tv_last_message.setText("");
            }
        } else {
            markwon.setMarkdown(tv_last_message, Global.getMentionedText(lastMessage));
        }

        if (lastMessage.isToday())
            tv_date.setText(lastMessage.getTime());
        else
            tv_date.setText(lastMessage.getDate() + ", " + lastMessage.getTime());

    }

    private void configLastMessageDate(ChannelResponse channelResponse, Message lastMessage) {
        if (Global.readMessage(channelResponse.getReadDateOfChannelLastMessage(true), lastMessage.getCreated_at())) {
            tv_last_message.setTypeface(tv_last_message.getTypeface(), Typeface.NORMAL);
            tv_last_message.setTextColor(context.getResources().getColor(R.color.gray_dark));
        } else {
            tv_last_message.setTypeface(tv_last_message.getTypeface(), Typeface.BOLD);
            tv_last_message.setTextColor(context.getResources().getColor(R.color.black));
        }
    }
}
