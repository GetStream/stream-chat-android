package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.BaseStyle;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel2;

public class ChannelListView extends RecyclerView {
    final String TAG = ChannelListView.class.getSimpleName();

    private Style style;

    // our connection to the channel scope
    private ChannelListViewModel viewModel;
    private UserClickListener userClickListener;
    private ChannelClickListener channelClickListener;
    private ChannelListItemAdapter adapter;

    public ChannelListView(Context context) {
        super(context);
    }

    public ChannelListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.parseAttr(context, attrs);
    }

    public ChannelListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.parseAttr(context, attrs);
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new Style(context, attrs);
    }

    public void setViewModel(ChannelListViewModel viewModel) {
        this.viewModel = viewModel;
        Client client = StreamChat.getInstance();

        // listen to events
        client.addEventHandler(new ChatEventHandler() {
            // TODO: actually support this
//            @Override
//            public void onChannelLoaded(Event event) {
//                adapter.addChannels(event.getChannels());
//            }
            @Override
            public void onNotificationMessageNew(Event event) {
                adapter.upsertChannel(event.getChannel());
            }

            @Override
            public void onChannelDeleted(Event event) {
                adapter.deleteChannel(event.getChannel());
            }

            @Override
            public void onChannelUpdated(Event event) {
                adapter.upsertChannel(event.getChannel());
            }
        });

        // setup the default adapter

        adapter = new ChannelListItemAdapter(getContext(), style.getLayoutID());
    }

    public void setOnUserClickListener(UserClickListener userClickListener) {
        this.userClickListener = userClickListener;
    }

    public void setOnChannelClickListener(ChannelClickListener channelClickListener) {
        this.channelClickListener = channelClickListener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    // set the adapter and apply the style.
    public void setAdapterWithStyle(ChannelListItemAdapter adapter) {
        super.setAdapter(adapter);
        adapter.setStyle(style);

        // 1. listen to the scroll
        // 2. call viewHolder.loadMore when at the top
        // 3. with the result of loadMore call adapter.addOldMessages()
        int fVPosition = ((LinearLayoutManager) this.getLayoutManager()).findFirstVisibleItemPosition();

        this.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int currentFirstVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (currentFirstVisible < fVPosition) {
                    //viewModel.loadMore();
                }
            }
        });

    }


    public class Style extends BaseStyle {
        final String TAG = Style.class.getSimpleName();
        // dimensions
        public float avatarWidth;
        public float avatarHeight;
        public float dateTextSize;
        // colors
        public int titleTextColor;
        public int unreadTitleTextColor;
        public int dateTextColor;
        // styles
        public int titleTextStyle;
        public int unreadTitleTextStyle;



        public Style(Context c, AttributeSet attrs) {
            this.setContext(c);
            TypedArray a = c.obtainStyledAttributes(attrs,
                    R.styleable.ChannelListView, 0, 0);

            avatarWidth = a.getDimension(R.styleable.ChannelListView_avatarWidth, c.getResources().getDimensionPixelSize(R.dimen.stream_channel_avatar_height));
            avatarHeight = a.getDimension(R.styleable.ChannelListView_avatarHeight, c.getResources().getDimensionPixelSize(R.dimen.stream_channel_avatar_width));
            dateTextSize = a.getDimension(R.styleable.ChannelListView_dateTextSize, c.getResources().getDimensionPixelSize(R.dimen.stream_channel_preview_date));

            titleTextColor = a.getColor(R.styleable.ChannelListView_titleTextColor, c.getResources().getColor(R.color.stream_channel_preview_title));
            unreadTitleTextColor = a.getColor(R.styleable.ChannelListView_unreadTitleTextColor, c.getResources().getColor(R.color.stream_channel_preview_last_message));
            dateTextColor = a.getColor(R.styleable.ChannelListView_dateTextColor, c.getResources().getColor(R.color.stream_channel_preview_date));

            titleTextStyle =a.getInt(R.styleable.ChannelListView_titleTextStyle, Typeface.NORMAL);
            unreadTitleTextStyle =a.getInt(R.styleable.ChannelListView_unreadTitleTextStyle, Typeface.NORMAL);

            a.recycle();
        }
    }


    public interface UserClickListener {
        void onClick(User user);
    }

    public interface ChannelClickListener {
        void onClick(Channel channel);
    }

}
