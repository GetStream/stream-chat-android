package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

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
        this.setLayoutManager(new LinearLayoutManager(context));
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.parseAttr(context, attrs);
        this.setLayoutManager(new LinearLayoutManager(context));
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.parseAttr(context, attrs);
        this.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;

        Channel c = this.viewModel.getChannel();
        Log.i(TAG, "MessageListView is attaching a listener on the channel object");


        // Setup a default adapter and pass the style
        adapter = new MessageListItemAdapter(getContext());

        // use livedata and observe
        viewModel.getMessages().observe(lifecycleOwner, messages -> {
            Log.i(TAG, "Observe found this many messages: " + messages.size());
            adapter.replaceMessages(messages);
        });

        this.setAdapterWithStyle(adapter);
    }
    // set the adapter and apply the style.
    public void setAdapterWithStyle(MessageListItemAdapter adapter) {
        super.setAdapter(adapter);
        adapter.setStyle(style);

        this.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (linearLayoutManager != null) {

                    int firstVisible = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                    Boolean reachedTheBeginning = firstVisible <= 3;
                    if (reachedTheBeginning) {
                        viewModel.loadMore();
                    }


                }

            }
        });

    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new MessageListViewStyle(context, attrs);
    }
}