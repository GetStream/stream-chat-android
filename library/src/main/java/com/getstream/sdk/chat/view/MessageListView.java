package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.adapter.Entity;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.List;


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
    private MessageViewHolderFactory viewHolderFactory;

    private MessageClickListener messageClickListener;
    private AttachmentClickListener attachmentClickListener;

    private int firstVisible;
    private int lastVisible;
    private boolean hasScrolledUp;

    public MessageListView(Context context) {
        super(context);
        this.setLayoutManager(new LinearLayoutManager(context));
        hasScrolledUp = false;
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.parseAttr(context, attrs);
        this.setLayoutManager(new LinearLayoutManager(context));
        hasScrolledUp = false;
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.parseAttr(context, attrs);
        this.setLayoutManager(new LinearLayoutManager(context));
        hasScrolledUp = false;
    }

    public void setViewHolderFactory(MessageViewHolderFactory factory) {
        this.viewHolderFactory = factory;
        if (this.adapter != null) {
            this.adapter.setFactory(factory);
        }
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
        if (viewHolderFactory != null) {
            adapter.setFactory(viewHolderFactory);
        }


        // use livedata and observe
        viewModel.getEntities().observe(lifecycleOwner, entityWrapper -> {
            List<Entity> entities = entityWrapper.getListEntities();
            Log.i(TAG, "Observe found this many entities: " + entities.size());
            int oldPosition = firstVisible;
            int oldSize = adapter.getItemCount();
            adapter.replaceEntities(entities);
            int newSize = adapter.getItemCount();
            int sizeGrewBy = newSize - oldSize;

            if (entityWrapper.getLoadingMore()) {
                // the load more behaviour is different, scroll positions starts out at 0
                // to stay at the relative 0 we should go to 0 + size of new messages...

                int newPosition = oldPosition + sizeGrewBy;
                this.getLayoutManager().scrollToPosition(newPosition);
                Log.i(TAG, String.format("Scroll: Loading more old position %d and new position %d", oldPosition, newPosition));
            } else {
                // regular new message behaviour
                // we scroll down all the way, unless you've scrolled up
                // if you've scrolled up we set a variable on the viewmodel that there are new messages
                Log.i(TAG, String.format("Scroll: Moving down"));

                if (!hasScrolledUp) {
                    this.getLayoutManager().scrollToPosition(adapter.getItemCount()-1);
                    viewModel.setHasNewMessages(false);
                } else {
                    viewModel.setHasNewMessages(true);
                }
                viewModel.markRead();
            }
        });

        this.setAdapterWithStyle(adapter);
    }

    // set the adapter and apply the style.
    public void setAdapterWithStyle(MessageListItemAdapter adapter) {
        super.setAdapter(adapter);
        adapter.setStyle(style);


        if (this.attachmentClickListener != null) {
            adapter.setAttachmentClickListener(this.attachmentClickListener);
        }
        if (this.messageClickListener != null) {
            this.adapter.setMessageClickListener(this.messageClickListener);
        }
        this.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (linearLayoutManager != null) {
                    firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
                    lastVisible = linearLayoutManager.findLastVisibleItemPosition();
                    hasScrolledUp = lastVisible < (adapter.getItemCount() -3);
                    if (!hasScrolledUp) {
                        viewModel.setHasNewMessages(false);
                    }
                    Boolean reachedTheBeginning = firstVisible <= 2;
                    Log.i(TAG, String.format("Scroll: First visible is %d last visible is %s", firstVisible, lastVisible));
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

    public void setAttachmentClickListener(AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
        if (this.adapter != null) {
            this.adapter.setAttachmentClickListener(this.attachmentClickListener);
        }
    }

    public void setMessageClickListener(MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;
        if (this.adapter != null) {
            this.adapter.setMessageClickListener(this.messageClickListener);
        }
    }

    public interface MessageClickListener {
        void onClick(Message message);
    }

    public interface AttachmentClickListener {
        void onClick(Message message, Attachment attachment);
    }
}