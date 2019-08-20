package com.getstream.sdk.chat.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.List;

import top.defaults.drawabletoolbox.DrawableBuilder;


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

    protected MessageListViewStyle style;
    private MessageListItemAdapter adapter;
    // our connection to the channel scope
    private ChannelViewModel viewModel;
    private MessageViewHolderFactory viewHolderFactory;

    private MessageClickListener messageClickListener;
    private LinearLayoutManager layoutManager;
    private MessageLongClickListener messageLongClickListener;
    private AttachmentClickListener attachmentClickListener;

    private int firstVisible;
    private int lastVisible;
    private boolean hasScrolledUp;
    private BubbleHelper bubbleHelper;

    // region Constructor
    public MessageListView(Context context) {
        super(context);
        init(context);
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.parseAttr(context, attrs);
        init(context);
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.parseAttr(context, attrs);
        init(context);
    }

    private void init(Context context) {
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true);
        this.setLayoutManager(layoutManager);
        hasScrolledUp = false;
        initDefaultBubbleHelper();
    }

    // endregion

    // region Init
    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new MessageListViewStyle(context, attrs);
    }

    public void initDefaultBubbleHelper() {
        this.setBubbleHelper(new BubbleHelper() {
            @Override
            public Drawable getDrawableForMessage(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions) {
                if (mine) {
                    // if the size is 0 the attachment has the corner change
                    if (positions.contains(MessageViewHolderFactory.Position.TOP) && message.getAttachments().size() == 0) {
                        return getResources().getDrawable(R.drawable.message_bubble_mine_top);
                    }
                    int conerRadius1 = getResources().getDimensionPixelSize(R.dimen.message_corner_radius1);
                    int conerRadius2 = getResources().getDimensionPixelSize(R.dimen.message_corner_radius2);
                    int bgColor = getResources().getColor(R.color.message_background_incoming);
                    int strokeColor = getResources().getColor(R.color.message_border);
                    int strokeWidth = getResources().getDimensionPixelSize(R.dimen.message_stroke_width);
                    return new DrawableBuilder()
                            .rectangle()
                            .strokeColor(strokeColor)
                            .strokeWidth(strokeWidth)
                            .solidColor(bgColor)
                            .cornerRadii(conerRadius2, conerRadius1, conerRadius1, conerRadius2)
                            .build();

//                    return style.getMessageBubbleDrawableMine();
                } else {
                    if (positions.contains(MessageViewHolderFactory.Position.TOP) && message.getAttachments().size() == 0) {
                        return getResources().getDrawable(R.drawable.message_bubble_theirs_top);
                    }
                    return style.getMessageBubbleDrawableTheirs();
                }
            }

            @Override
            public Drawable getDrawableForAttachment(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions, Attachment attachment) {
//                if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
//                    int attachmentPosition = message.getAttachments().indexOf(attachment);
//                    if (attachmentPosition == 0) {
//                        return getResources().getDrawable(R.drawable.round_attach_media_incoming1);
//                    }
//                }
                int conerRadius1 = getResources().getDimensionPixelSize(R.dimen.message_corner_radius1);
                int conerRadius2 = getResources().getDimensionPixelSize(R.dimen.message_corner_radius2);
                int bgColor = getResources().getColor(R.color.message_background_incoming);
                int strokeColor = getResources().getColor(R.color.message_border);
                int strokeWidth = getResources().getDimensionPixelSize(R.dimen.message_stroke_width);
                return new DrawableBuilder()
                        .rectangle()
                        .strokeColor(strokeColor)
                        .strokeWidth(strokeWidth)
                        .solidColor(bgColor)
                        .cornerRadii(conerRadius2, conerRadius1, conerRadius1, conerRadius2)
                        .build();
//                return getResources().getDrawable(R.drawable.round_attach_media_incoming2);
            }
        });
    }

    private void initFresco() {
        try {
            Fresco.initialize(getContext());
        } catch (Exception e) {
        }

    }

    // set the adapter and apply the style.
    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    public void setAdapterWithStyle(MessageListItemAdapter adapter) {

        adapter.setStyle(style);

        if (this.attachmentClickListener != null) {
            adapter.setAttachmentClickListener(this.attachmentClickListener);
        }
        if (this.messageClickListener != null) {
            this.adapter.setMessageClickListener(this.messageClickListener);
        }
        if (this.messageLongClickListener != null) {
            this.adapter.setMessageLongClickListener(this.messageLongClickListener);
        }
        this.adapter.setChannelState(getChannel().getChannelState());

        this.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                if (layoutManager != null) {
                    firstVisible = layoutManager.findFirstVisibleItemPosition();
                    lastVisible = layoutManager.findLastVisibleItemPosition();
                    hasScrolledUp = lastVisible < (adapter.getItemCount() - 3);
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
        super.setAdapter(adapter);
    }

    public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;
        initFresco();
        Channel c = this.viewModel.getChannel();
        Log.i(TAG, "MessageListView is attaching a listener on the channel object");


        // Setup a default adapter and pass the style
        adapter = new MessageListItemAdapter(getContext());
        if (viewHolderFactory != null) {
            adapter.setFactory(viewHolderFactory);
        }

        if (bubbleHelper != null) {
            adapter.setBubbleHelper(bubbleHelper);
        }


        // use livedata and observe
        viewModel.getEntities().observe(lifecycleOwner, messageListItemWrapper -> {
            List<MessageListItem> entities = messageListItemWrapper.getListEntities();
            Log.i(TAG, "Observe found this many entities: " + entities.size());
            int oldPosition = firstVisible;
            int oldSize = adapter.getItemCount();
            adapter.replaceEntities(entities);
            int newSize = adapter.getItemCount();
            int sizeGrewBy = newSize - oldSize;

            if (oldSize == 0 && newSize != 0) {
                int newPosition = adapter.getItemCount() - 1;
                layoutManager.scrollToPosition(newPosition);
                Log.i(TAG, String.format("Scroll: First load scrolling down to bottom %d", newPosition));
            } else if (messageListItemWrapper.getLoadingMore()) {
                // the load more behaviour is different, scroll positions starts out at 0
                // to stay at the relative 0 we should go to 0 + size of new messages...

                int newPosition = oldPosition + sizeGrewBy;
                layoutManager.scrollToPosition(newPosition);
                Log.i(TAG, String.format("Scroll: Loading more old position %d and new position %d", oldPosition, newPosition));
            } else {
                if (newSize == 0) return;
                // regular new message behaviour
                // we scroll down all the way, unless you've scrolled up
                // if you've scrolled up we set a variable on the viewmodel that there are new messages
                int newPosition = adapter.getItemCount() - 1;
                int layoutSize = layoutManager.getItemCount();
                Log.i(TAG, String.format("Scroll: Moving down to %d, layout has %d elements", newPosition, layoutSize));

                if (!hasScrolledUp) {
                    layoutManager.scrollToPosition(newPosition);
                    viewModel.setHasNewMessages(false);
                } else {
                    viewModel.setHasNewMessages(true);
                }
                // TODO: this is an infinite loop: read event -> mark read -> read event
                // we want to mark read if there is a new message
                // and this view is currently being displayed...
                // we can't always run it since read and typing events also influence this list..
                viewModel.markLastMessageRead();
            }
        });

        this.setAdapterWithStyle(adapter);
    }

    public void setViewHolderFactory(MessageViewHolderFactory factory) {
        this.viewHolderFactory = factory;
        if (this.adapter != null) {
            this.adapter.setFactory(factory);
        }
    }

    public Channel getChannel() {
        if (viewModel != null)
            return viewModel.getChannel();
        return null;
    }

    public MessageListViewStyle getStyle() {
        return style;
    }
    // endregion


    // region Listener
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

    public void setMessageLongClickListener(MessageLongClickListener messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    public void setBubbleHelper(BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
        if (adapter != null) {
            adapter.setBubbleHelper(bubbleHelper);
        }
    }

    public interface MessageClickListener {
        void onMessageClick(Message message, int position);
    }

    public interface MessageLongClickListener {
        void onMessageLongClick(Message message);
    }

    public interface BubbleHelper {
        Drawable getDrawableForMessage(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions);

        Drawable getDrawableForAttachment(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions, Attachment attachment);
    }

    public interface AttachmentClickListener {
        void onAttachmentClick(Message message, Attachment attachment);
    }
    // endregion
}