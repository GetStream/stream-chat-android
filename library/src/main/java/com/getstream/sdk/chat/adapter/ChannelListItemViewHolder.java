package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.ReadIndicator;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.AvatarGroupView;
import com.getstream.sdk.chat.view.ChannelListView;
import com.getstream.sdk.chat.view.ReadStateView;

import java.text.SimpleDateFormat;
import java.util.List;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import ru.noties.markwon.image.ImagesPlugin;

import static android.text.format.DateUtils.getRelativeTimeSpanString;

public class ChannelListItemViewHolder extends BaseChannelListItemViewHolder {

    public ConstraintLayout cl_root;
    public TextView tv_name, tv_last_message, tv_date, tv_click;
    public ReadStateView read_state;
    public ImageView iv_indicator;
    public AvatarGroupView iv_avatar;

    private Context context;

    private Markwon markdownBuilder;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private ChannelListView.Style style;
    
    public ChannelListItemViewHolder(int resId, ViewGroup parent, ChannelListView.Style style) {
        super(resId, parent);


        this.style = style;
        findReferences();
        applyStyle();
    }

    public void findReferences() {
        cl_root = itemView.findViewById(R.id.cl_root);
        tv_name = itemView.findViewById(R.id.tv_name);
        tv_last_message = itemView.findViewById(R.id.tv_last_message);
        tv_date = itemView.findViewById(R.id.tv_date);

        tv_click = itemView.findViewById(R.id.tv_click);
        iv_avatar = itemView.findViewById(R.id.avatar_group);

        read_state = itemView.findViewById(R.id.read_state);

    }

    // TODO:
    public void applyStyle() {
        // TODO: apply more styles here
        //tv_date.setTextSize(style.dateTextSize);
    }

    public void applyUnreadStyle() {
        // TODO: apply more styles here
        //tv_date.setTextSize(style.dateTextSize);
    }

    public void applyReadStyle() {
        // TODO: apply more styles here
        //tv_date.setTextSize(style.dateTextSize);
    }

    @Override
    public void bind(Context context, ChannelState channelState, int position,
                     View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {

        // setup the click listeners and the markdown builder
        this.context = context;
        if (markdownBuilder == null)
            this.markdownBuilder = Markwon.builder(context)
                    .usePlugin(ImagesPlugin.create(context))
                    .usePlugin(CorePlugin.create())
                    .usePlugin(StrikethroughPlugin.create())
                    .build();
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;

        // the UI depends on the
        // - lastMessage
        // - unread count
        // - read state for this channel
        Message lastMessage = channelState.getLastMessage();
        int unreadCount = channelState.getCurrentUserUnreadMessageCount();
        List<ChannelUserRead> lastMessageReads = channelState.getLastMessageReads();
        List<User> otherUsers = channelState.getOtherUsers();
        String channelName = channelState.getChannelNameOrMembers();

        // set the data for the avatar
        iv_avatar.setChannelAndOtherUsers(channelState.getChannel(), otherUsers);

        // set the channel name
        tv_name.setText(channelName);

        // set the lastMessage and last messageDate
        SimpleDateFormat format = new SimpleDateFormat("MMM d");
        if (lastMessage != null) {
            // humanized time diff
//            tv_last_message.setText(lastMessage.getText());
//            String humanizedDateDiff = getRelativeTimeSpanString(lastMessage.getCreatedAtDate().getTime()).toString();
//            tv_date.setText(humanizedDateDiff);

            if (lastMessage.isToday())
                tv_date.setText(lastMessage.getTime());
            else
                tv_date.setText(format.format(lastMessage.getCreatedAtDate()));
        }




        // read indicators
        read_state.setReads(lastMessageReads);

        // apply unread style or read style
        if (unreadCount == 0) {
            this.applyReadStyle();
        } else {
            this.applyUnreadStyle();
        }

        // click listeners
        // TODO: clicking an individual user avatar... usually has a different behaviour...
        tv_click.setOnClickListener(view -> {
            Utils.setButtonDelayEnable(view);
            view.setTag(channelState.getChannel().getCid());

            tv_click.setBackgroundColor(Color.parseColor("#14000000"));
            new Handler().postDelayed(() ->tv_click.setBackgroundColor(0), 500);
            this.clickListener.onClick(view);
        });

        tv_click.setOnLongClickListener(view -> {
            view.setTag(channelState.getChannel().getCid());
            this.longClickListener.onLongClick(view);
            return true;
        });
    }
}
