package com.getstream.sdk.chat.view.channels;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.adapter.ChannelViewHolderFactory;

import java.util.List;

import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.User;


public class ChannelListView extends RecyclerView {

    private ChannelListViewStyle style;
    // our connection to the channel scope
    private UserClickListener userClickListener;
    private ChannelClickListener channelClickListener;
    private ChannelClickListener channelLongClickListener;
    private EndReachedListener endReachedListener;
    private ChannelListItemAdapter adapter;
    private ChannelViewHolderFactory viewHolderFactory;

    private LinearLayoutManager layoutManager;
    private final EndReachedScrollListener scrollListener = new EndReachedScrollListener();

    public ChannelListView(Context context) {
        super(context);
        this.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        this.setLayoutManager(layoutManager);
        init();
    }

    public ChannelListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        this.setLayoutManager(layoutManager);
        this.parseAttr(context, attrs);
        init();
    }

    public ChannelListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        this.setLayoutManager(layoutManager);
        this.parseAttr(context, attrs);
        init();
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new ChannelListViewStyle(context, attrs);

        // use the background color as a default for the avatar border
        if (style.getAvatarBorderColor() == -1) {
            int color = Color.WHITE;
            Drawable background = this.getBackground();
            if (background instanceof ColorDrawable)
                color = ((ColorDrawable) background).getColor();
            style.setAvatarBorderColor(color);
        }
    }

    private void init() {
        this.adapter = new ChannelListItemAdapter(getContext());
        this.setAdapterWithStyle(adapter);
        adapter.setChannelClickListener(this.channelClickListener);
        setOnLongClickListener(this.channelLongClickListener);
        adapter.setUserClickListener(this.userClickListener);
    }

    private boolean canScrollUpForChannelEvent() {
        return layoutManager.findFirstVisibleItemPosition() < 3;
    }

    public void setViewHolderFactory(ChannelViewHolderFactory factory) {
        this.viewHolderFactory = factory;
        if (this.adapter != null) {
            this.adapter.setViewHolderFactory(factory);
        }
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
        if (adapter == null) return;

        if (l != null) {
            adapter.setChannelLongClickListener(l);
        }
    }

    public void setOnEndReachedListener(EndReachedListener listener) {
        this.endReachedListener = listener;
        observeListEndRegion();
    }

    private void observeListEndRegion() {
        this.addOnScrollListener(scrollListener);
    }

    public void setPaginationEnabled(boolean enabled) {
        scrollListener.setPaginationEnabled(enabled);
    }

    public void setChannels(final List<Channel> channels) {
        adapter.replaceChannels(channels);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    @Override
    public void onVisibilityChanged(View view, int visibility) {
        super.onVisibilityChanged(view, visibility);
        if (visibility == 0 && adapter != null)
            adapter.notifyDataSetChanged();
    }

    // set the adapter and apply the style.
    public void setAdapterWithStyle(ChannelListItemAdapter adapter) {
        super.setAdapter(adapter);
        adapter.setStyle(style);

        if (viewHolderFactory != null) {
            adapter.setViewHolderFactory(viewHolderFactory);
        }
    }

    public interface UserClickListener {
        void onUserClick(User user);
    }

    public interface ChannelClickListener {
        void onClick(Channel channel);
    }

    public interface EndReachedListener {
        void onEndReached();
    }

    private class EndReachedScrollListener extends RecyclerView.OnScrollListener {
        private boolean enabled = false;

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (SCROLL_STATE_IDLE == newState) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
                final int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                boolean reachedTheEnd = (adapter.getItemCount() - 1) == lastVisiblePosition;
                if (reachedTheEnd && enabled) {
                    endReachedListener.onEndReached();
                }
            }
        }

        public void setPaginationEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
