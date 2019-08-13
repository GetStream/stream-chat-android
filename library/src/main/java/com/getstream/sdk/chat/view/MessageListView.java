package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;


/**
 * MessageListView renders a list of messages and extends the RecyclerView
 * The most common customizations are
 * - Disabling Reactions
 * - Disabling Threads
 * - Customizing the click and longCLick (via the adapter)
 * - The list_item_message template to use (perhaps, multiple ones...?)
 */
public class MessageListView extends RecyclerView {
    final String TAG = MessageListView.class.getSimpleName();

    private MessageListViewStyle style;
    private MessageListItemAdapter adapter;
    // our connection to the channel scope
    private ChannelViewModel viewModel;
    public MessageListView(Context context) {
        super(context);
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.parseAttr(context, attrs);
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.parseAttr(context, attrs);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    //TODO: binding.setLifecycleOwner(lifecycleOwner);
    public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;

        Channel c = this.viewModel.getChannel();
        Log.i(TAG, "MessageListView is attaching a listener on the channel object");
        c.addEventHandler(new ChatChannelEventHandler() {
            // TODO
            // - onLoadMore event
            // - onMessageNew should fire before API call is completed (or have a different event for that)
            // - perhaps onMessageLocalNew
            @Override
            public void onMessageNew(Event event) {
                Log.i(TAG, "MessageListView received onMessageNew event");
                // forward to the adapter
                adapter.addNewMessage(event.getMessage());
            }
//            @Override
//            public void onMessageUpdated(Event event) {
//                Log.i(TAG, "MessageListView received onMessageNew event");
//                // forward to the adapter
//                adapter.addNewMessage(event.getMessage());
//            }
//            @Override
//            public void onMessageDeleted(Event event) {
//                Log.i(TAG, "MessageListView received onMessageNew event");
//                // forward to the adapter
//                adapter.addNewMessage(event.getMessage());
//            }
//            @Override
//            public void onLoadMore(Event event) {
//                Log.i(TAG, "MessageListView received onLoadMore event");
//                // forward to the adapter
//                adapter.addOldMessages(event.getMessages());
//            }

        });
    }
    // set the adapter and apply the style.
    public void setAdapterWithStyle(MessageListItemAdapter adapter) {
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
                    viewModel.loadMore();
                }
            }
        });

    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new MessageListViewStyle(context, attrs);
    }
}