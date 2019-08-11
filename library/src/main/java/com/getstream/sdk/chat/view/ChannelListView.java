package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.utils.BaseStyle;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

public class ChannelListView extends RecyclerView {
    final String TAG = ChannelListView.class.getSimpleName();

    private Style style;

    // our connection to the channel scope
    private ChannelListViewModel viewModel;
    private UserClickListener userClickListener;
    private ChannelClickListener channelClickListener;
    private ChannelClickListener channelLongClickListener;
    private ChannelListItemAdapter adapter;

    public ChannelListView(Context context) {
        super(context);
        this.setHasFixedSize(true);
    }

    public ChannelListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setHasFixedSize(true);
        this.parseAttr(context, attrs);
    }

    public ChannelListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setHasFixedSize(true);
        this.parseAttr(context, attrs);
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new Style(context, attrs);
    }

    // TODO: binding.setLifecycleOwner(lifecycleOwner);
    public void setViewModel(ChannelListViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;

        // setup the default adapter
        adapter = new ChannelListItemAdapter(getContext(), R.layout.list_item_channel);
        this.setAdapterWithStyle(adapter);

        // connect the viewHolder on click listener...
        adapter.setChannelClickListener(this.channelClickListener);
        adapter.setChannelLongClickListener(this.channelLongClickListener);
        adapter.setUserClickListener(this.userClickListener);

        // listen to events
        viewModel.client().addEventHandler(new ChatEventHandler() {
            @Override
            public void onNotificationMessageNew(Event event) {
                Message lastMessage = event.getChannel().getChannelState().getLastMessage();
                Log.i(TAG, "Received a new message with text: " + event.getMessage().getText());
                Log.i(TAG, "Last message is: " + lastMessage.getText());
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

            @Override
            public void onMessageRead(Event event) {
                adapter.upsertChannel(event.getChannel());
            }
        });

        // TODO: this approach is not great for performance
        viewModel.getChannels().observe(lifecycleOwner, channels -> adapter.replaceChannels(channels));
    }

    public void setOnUserClickListener(UserClickListener l) {
        this.userClickListener = l;
        if (adapter != null) {
            adapter.setUserClickListener(l);
        }
    }

    public void setOnChannelClickListener(ChannelClickListener l) {
        this.channelClickListener = l;
        if (adapter != null) {
            adapter.setChannelClickListener(l);
        }
    }

    public void setOnLongClickListener(ChannelClickListener l) {
        this.channelLongClickListener = l;
        if (adapter != null) {
            adapter.setChannelLongClickListener(l);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    // set the adapter and apply the style.
    public void setAdapterWithStyle(ChannelListItemAdapter adapter) {
        super.setAdapter(adapter);
        adapter.setStyle(style);

        this.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (linearLayoutManager != null) {
                    Boolean reachedTheEnd = linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1;
                    // the viewmodel prevents triggering this all the time
                    viewModel.loadMore();
                    Log.i(TAG, "loading more");
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

            titleTextStyle =a.getInt(R.styleable.ChannelListView_titleTextStyleChannel, Typeface.NORMAL);
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
