package com.getstream.sdk.chat.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.ChannelListItemAdapter;
import com.getstream.sdk.chat.adapter.ChannelViewHolderFactory;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;


public class ChannelListView extends RecyclerView {
    final String TAG = ChannelListView.class.getSimpleName();

    private ChannelListViewStyle style;
    // our connection to the channel scope
    private ChannelListViewModel viewModel;
    private UserClickListener userClickListener;
    private ChannelClickListener channelClickListener;
    private ChannelClickListener channelLongClickListener;
    private ChannelListItemAdapter adapter;
    private ChannelViewHolderFactory viewHolderFactory;

    private LinearLayoutManager layoutManager;

    public ChannelListView(Context context) {
        super(context);
        this.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        this.setLayoutManager(layoutManager);
    }

    public ChannelListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        this.setLayoutManager(layoutManager);
        this.parseAttr(context, attrs);

    }

    public ChannelListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        this.setLayoutManager(layoutManager);
        this.parseAttr(context, attrs);
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

    public void setViewModel(ChannelListViewModel viewModel, LifecycleOwner lifecycleOwner, ChannelListItemAdapter adapter) {
        this.viewModel = viewModel;

        // connect the adapter
        this.adapter = adapter;
        this.setAdapterWithStyle(adapter);

        // connect the viewHolder on click listener...
        adapter.setChannelClickListener(this.channelClickListener);
        setOnLongClickListener(this.channelLongClickListener);
        adapter.setUserClickListener(this.userClickListener);

        viewModel.getChannels().observe(lifecycleOwner, channels -> {
            StreamChat.getLogger().logI(this,"Observe found this many channels: " + channels.size());
            adapter.replaceChannels(channels);
            if (canScrollUpForChannelEvent())
                layoutManager.scrollToPosition(0);
        });
    }

    private boolean canScrollUpForChannelEvent(){
        return layoutManager.findFirstVisibleItemPosition() < 3;
    }

    public void setViewHolderFactory(ChannelViewHolderFactory factory) {
        this.viewHolderFactory = factory;
        if (this.adapter != null) {
            this.adapter.setViewHolderFactory(factory);
        }
    }

    public void setViewModel(ChannelListViewModel viewModel, LifecycleOwner lifecycleOwner) {
        // default adapter...
        adapter = new ChannelListItemAdapter(getContext());

        this.setViewModel(viewModel, lifecycleOwner, adapter);
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

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    @Override
    public void onVisibilityChanged(View view, int visibility){
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

        this.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (linearLayoutManager != null) {
                    int lastVisible = linearLayoutManager.findLastVisibleItemPosition();
                    boolean reachedTheEnd = lastVisible == adapter.getItemCount() - 1;
                    // the viewmodel ensures that we only load once..
                    if (reachedTheEnd) viewModel.loadMore();
                }
            }
        });
    }

    public interface UserClickListener {
        void onUserClick(User user);
    }

    public interface ChannelClickListener {
        void onClick(Channel channel);
    }
}
