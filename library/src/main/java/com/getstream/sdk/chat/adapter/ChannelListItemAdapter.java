package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ListItemChannelBinding;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.model.message.Attachment;
import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;

import java.util.List;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import ru.noties.markwon.image.ImagesPlugin;

public class ChannelListItemAdapter extends BaseAdapter {

    private final String TAG = ChannelListItemAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private Context context;
    private List<ChannelResponse> channels;
    private Markwon markwon;
    private View.OnClickListener clickListener;

    public ChannelListItemAdapter(Context context, List channels, View.OnClickListener clickListener) {
        this.context = context;
        this.channels = channels;
        this.layoutInflater = LayoutInflater.from(context);
        this.clickListener = clickListener;
        this.markwon = Markwon.builder(context)
                .usePlugin(ImagesPlugin.create(context))
                .usePlugin(CorePlugin.create())
                .usePlugin(StrikethroughPlugin.create())
                .build();
    }

    @Override
    public int getCount() {
        return channels.size();
    }

    @Override
    public Object getItem(int position) {
        return channels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ListItemChannelBinding binding;
        ListItemChannelBinding binding1;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_channel, null);
            binding1 = DataBindingUtil.bind(convertView);
            convertView.setTag(binding1);
        } else {
            try {
                binding1 = (ListItemChannelBinding) convertView.getTag();
            } catch (Exception e) {
                convertView = layoutInflater.inflate(R.layout.list_item_channel, null);
                binding1 = DataBindingUtil.bind(convertView);
            }
        }

        binding = binding1;
        ChannelResponse channelResponse = channels.get(position);

        Message lastMessage = channelResponse.getLastMessage();
        configUIs(binding, position);
        configChannelInfo(binding, channelResponse);
        configIndicatorUserInfo(binding, channelResponse);
        if (lastMessage != null) {
            configMessageDate(binding, lastMessage);
            configLastMessageDate(binding, channelResponse, lastMessage);
        }

        binding.tvClick.setOnClickListener(view -> {
            view.setTag(position);
            binding.tvClick.setBackgroundColor(context.getResources().getColor(R.color.mesage_border));
            ChannelListItemAdapter.this.clickListener.onClick(view);
        });
        return binding.getRoot();
    }

    private void configUIs(ListItemChannelBinding binding, int position) {
        binding.tvClick.setBackgroundColor(0);
        if (position == 0)
            binding.clRoot.setBackgroundResource(R.drawable.round_channel_list_new);
        else
            binding.clRoot.setBackgroundResource(0);
    }

    private void configChannelInfo(ListItemChannelBinding binding, ChannelResponse channelResponse) {
        Channel channel = channelResponse.getChannel();
        if (!TextUtils.isEmpty(channel.getName())) {
            binding.tvInitials.setText(channel.getInitials());
            binding.tvName.setText(channel.getName());
            Utils.circleImageLoad(binding.ivAvatar, channel.getImageURL());
            if (StringUtility.isValidImageUrl(channel.getImageURL())) {
                binding.ivAvatar.setVisibility(View.VISIBLE);
                binding.tvInitials.setVisibility(View.INVISIBLE);
            } else {
                binding.ivAvatar.setVisibility(View.INVISIBLE);
                binding.tvInitials.setVisibility(View.VISIBLE);
            }
        } else {
            User opponent = Global.getOpponentUser(channelResponse);
            binding.tvInitials.setText(opponent.getUserInitials());
            if (opponent != null) {
                Utils.circleImageLoad(binding.ivAvatar, opponent.getImage());
                binding.tvName.setText(opponent.getName());
                binding.tvInitials.setVisibility(View.VISIBLE);
                binding.ivAvatar.setVisibility(View.VISIBLE);
            } else {
                binding.tvInitials.setVisibility(View.VISIBLE);
                binding.ivAvatar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void configIndicatorUserInfo(ListItemChannelBinding binding, ChannelResponse channelResponse) {
        User lastReadUser = channelResponse.getLastReadUser();
        if (lastReadUser != null) {
            if (StringUtility.isValidImageUrl(lastReadUser.getImage())) {
                Utils.circleImageLoad(binding.ivIndicator, lastReadUser.getImage());
                binding.ivIndicator.setVisibility(View.VISIBLE);
                binding.tvIndicatorInitials.setVisibility(View.INVISIBLE);
            } else {
                binding.tvIndicatorInitials.setText(lastReadUser.getUserInitials());
                binding.tvIndicatorInitials.setVisibility(View.VISIBLE);
                binding.ivIndicator.setVisibility(View.INVISIBLE);
            }
        } else {
            binding.tvIndicatorInitials.setVisibility(View.INVISIBLE);
            binding.ivIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void configMessageDate(ListItemChannelBinding binding, Message lastMessage) {
        ;
        if (TextUtils.isEmpty(lastMessage.getText())) {
            if (!lastMessage.getAttachments().isEmpty()) {
                Attachment attachment = lastMessage.getAttachments().get(0);
                binding.tvLastMessage.setText(!TextUtils.isEmpty(attachment.getTitle()) ? attachment.getTitle() : attachment.getFallback());
            } else {
                binding.tvLastMessage.setText("");
            }
        } else {
            markwon.setMarkdown(binding.tvLastMessage, Global.getMentionedText(lastMessage));
        }

        if (lastMessage.isToday())
            binding.tvDate.setText(lastMessage.getTime());
        else
            binding.tvDate.setText(lastMessage.getDate() + ", " + lastMessage.getTime());

    }

    private void configLastMessageDate(ListItemChannelBinding binding, ChannelResponse channelResponse, Message lastMessage) {
        if (Global.readMessage(channelResponse.getReadDateOfChannelLastMessage(true), lastMessage.getCreated_at())) {
            binding.tvLastMessage.setTypeface(binding.tvLastMessage.getTypeface(), Typeface.NORMAL);
            binding.tvLastMessage.setTextColor(context.getResources().getColor(R.color.gray_dark));
        } else {
            binding.tvLastMessage.setTypeface(binding.tvLastMessage.getTypeface(), Typeface.BOLD);
            binding.tvLastMessage.setTextColor(context.getResources().getColor(R.color.black));
        }
    }
}
