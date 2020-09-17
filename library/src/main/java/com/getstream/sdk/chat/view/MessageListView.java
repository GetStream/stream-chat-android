package com.getstream.sdk.chat.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.DefaultBubbleHelper;
import com.getstream.sdk.chat.adapter.ListenerContainer;
import com.getstream.sdk.chat.adapter.ListenerContainerImpl;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.enums.GiphyAction;
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.dialog.MessageMoreActionDialog;
import com.getstream.sdk.chat.view.dialog.ReadUsersDialog;
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper;

import java.util.Date;
import java.util.List;

import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

/**
 * MessageListView renders a list of messages and extends the RecyclerView
 * The most common customizations are
 * - Disabling Reactions
 * - Disabling Threads
 * - Customizing the click and longCLick (via the adapter)
 * - The list_item_message template to use (perhaps, multiple ones...?)
 */
public class MessageListView extends RecyclerView {
    //    private int firstVisible;
    private static int fVPosition, lVPosition;
    protected MessageListViewStyle style;

    @Nullable
    private MessageListItemAdapter adapter = null;
    private LinearLayoutManager layoutManager;

    private boolean hasScrolledUp;
    private int threadParentPosition;
    private Function0<Unit> endRegionReachedHandler = () -> {
        throw new IllegalStateException("endRegionReachedHandler must be set.");
    };
    private Function0<Unit> lastMessageReadHandler = () -> {
        throw new IllegalStateException("lastMessageReadHandler must be set.");
    };
    private Function1<Message, Unit> onMessageEditHandler = (Message m) -> {
        throw new IllegalStateException("onMessageEditHandler must be set.");
    };
    private Function1<Message, Unit> onMessageDeleteHandler = (Message m) -> {
        throw new IllegalStateException("onMessageDeleteHandler must be set.");
    };
    private Function1<Message, Unit> onStartThreadHandler = (Message m) -> {
        throw new IllegalStateException("onStartThreadHandler must be set.");
    };
    private Function1<Message, Unit> onStartThreadListener = (Message m) -> Unit.INSTANCE;
    private Function1<Message, Unit> onMessageFlagHandler = (Message m) -> {
        throw new IllegalStateException("onMessageFlagHandler must be set.");
    };
    private Function2<Message, GiphyAction, Unit> onSendGiphyHandler = (Message m, GiphyAction a) -> {
        throw new IllegalStateException("onSendGiphyHandler must be set.");
    };
    private Channel channel;
    private User currentUser;
    /**
     * If you are allowed to scroll up or not
     */
    boolean lockScrollUp = true;

    private TaggedLogger logger = ChatLogger.Companion.get("MessageListView");

    private final MessageClickListener DEFAULT_MESSAGE_CLICK_LISTENER = (message, position) -> {
        if (message.getReplyCount() > 0) {
            onStartThreadHandler.invoke(message);
            onStartThreadListener.invoke(message);
        } else {
            //viewModel.sendMessage(message);
        }
    };
    private final MessageLongClickListener DEFAULT_MESSAGE_LONG_CLICK_LISTENER = message -> {
        new MessageMoreActionDialog(
                getContext(),
                channel,
                message,
                currentUser,
                style,
                onMessageEditHandler,
                onMessageDeleteHandler,
                (Message m) -> {
                    onStartThreadHandler.invoke(m);
                    onStartThreadListener.invoke(m);
                    return Unit.INSTANCE;
                },
                onMessageFlagHandler
        ).show();
    };
    private final AttachmentClickListener DEFAULT_ATTACHMENT_CLICK_LISTENER = (message, attachment) -> {
        Chat.getInstance()
                .getNavigator()
                .navigate(new AttachmentDestination(message, attachment, getContext()));
    };
    private final ReactionViewClickListener DEFAULT_REACTION_VIEW_CLICK_LISTENER = message -> {
        Utils.hideSoftKeyboard((Activity) getContext());
        new MessageMoreActionDialog(
                getContext(),
                channel,
                message,
                currentUser,
                style,
                onMessageEditHandler,
                onMessageDeleteHandler,
                onStartThreadHandler,
                onMessageFlagHandler
        ).show();
    };
    private final UserClickListener DEFAULT_USER_CLICK_LISTENER = user -> { /* Empty */ };
    private final ReadStateClickListener DEFAULT_READ_STATE_CLICK_LISTENER = reads -> {
        new ReadUsersDialog(getContext())
                .setReads(reads)
                .setStyle(style)
                .show();
    };
    private final GiphySendListener DEFAULT_GIPHY_SEND_LISTENER = (message, action) -> {
        onSendGiphyHandler.invoke(message, action);
    };

    private final ListenerContainer listenerContainer = new ListenerContainerImpl(
            DEFAULT_MESSAGE_CLICK_LISTENER,
            DEFAULT_MESSAGE_LONG_CLICK_LISTENER,
            DEFAULT_ATTACHMENT_CLICK_LISTENER,
            DEFAULT_REACTION_VIEW_CLICK_LISTENER,
            DEFAULT_USER_CLICK_LISTENER,
            DEFAULT_READ_STATE_CLICK_LISTENER,
            DEFAULT_GIPHY_SEND_LISTENER
    );

    private BubbleHelper bubbleHelper;
    private MessageViewHolderFactory viewHolderFactory;

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
        setHasFixedSize(true);
        setItemViewCacheSize(20);
    }

    // region Init
    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new MessageListViewStyle(context, attrs);
    }

    // set the adapter and apply the style.
    @Override
    @Deprecated
    public final void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setMessageListItemAdapter instead please");
    }

    private void setMessageListItemAdapter(@NonNull MessageListItemAdapter adapter) {
        this.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (layoutManager != null) {
                    int currentFirstVisible = layoutManager.findFirstVisibleItemPosition();
                    int currentLastVisible = layoutManager.findLastVisibleItemPosition();
                    if (currentFirstVisible < fVPosition && currentFirstVisible == 0) {
                        endRegionReachedHandler.invoke();
                    }

                    hasScrolledUp = currentLastVisible <= (adapter.getItemCount() - 3); //TODO: investigate magic number 3
                    lVPosition = currentLastVisible;
                    fVPosition = currentFirstVisible;
                    threadParentPosition = lVPosition;
                }
            }
        });

        /*
         * Lock for 500 milliseconds setMessageListScrollUp in here.
         * Because when keyboard shows up, MessageList is scrolled up and it triggers hiding keyboard.
         */

        this.addOnLayoutChangeListener((View view, int left, int top, int right, int bottom,
                                        int oldLeft, int oldTop, int oldRight, int oldBottom) -> {
            if (bottom < oldBottom) {
                lockScrollUp = true;
                postDelayed(() -> lockScrollUp = false, 500);
            }
        });
        super.setAdapter(adapter);
    }

    public void init(Channel channel, User currentUser) {
        this.currentUser = currentUser;
        this.channel = channel;
        initAdapter();
    }

    private void initAdapter() {
        // Set default ViewHolderFactory if needed
        if (viewHolderFactory == null) {
            viewHolderFactory = new MessageViewHolderFactory();
        }
        // Inject factory
        viewHolderFactory.setListenerContainer(listenerContainer);

        // Set default BubbleHelper if needed
        if (bubbleHelper == null) {
            bubbleHelper = DefaultBubbleHelper.initDefaultBubbleHelper(style, getContext());
        }

        adapter = new MessageListItemAdapter(getContext(), channel, viewHolderFactory, bubbleHelper, style);
        adapter.setHasStableIds(true);

        this.setMessageListItemAdapter(adapter);
    }

    public void setViewHolderFactory(MessageViewHolderFactory factory) {
        if (adapter != null) {
            throw new IllegalStateException("Adapter was already inited, please set ViewHolderFactory first");
        }
        this.viewHolderFactory = factory;
    }

    public void setBubbleHelper(BubbleHelper bubbleHelper) {
        if (adapter != null) {
            throw new IllegalStateException("Adapter was already inited, please set BubbleHelper first");
        }
        this.bubbleHelper = bubbleHelper;
    }

    public void displayNewMessage(MessageListItemWrapper listItem) {
        List<MessageListItem> entities = listItem.getListEntities();

        // Adapter initialization for channel and thread swapping
        boolean backFromThread = false;
        if (adapter.isThread() != listItem.isThread()) {
            adapter.setThread(listItem.isThread());
            backFromThread = !listItem.isThread();
        }

        int oldSize = adapter.getItemCount();
        adapter.replaceEntities(entities);

        // Scroll to origin position on return from thread
        if (backFromThread) {
            layoutManager.scrollToPosition(this.threadParentPosition);
            lastMessageReadHandler.invoke();
            return;
        }

        // Scroll to bottom position for typing indicator
        if (listItem.isTyping() && scrolledBottom()) {
            int newPosition = adapter.getItemCount() - 1;
            layoutManager.scrollToPosition(newPosition);
            return;
        }
        // check lastmessage update
        if (!entities.isEmpty()) {
            final MessageListItem lastListItem = entities.get(entities.size() - 1);
            if (lastListItem instanceof MessageListItem.MessageItem) {
                final Message lastMessage = ((MessageListItem.MessageItem) lastListItem).getMessage();
                // Checks if we should scroll to bottom because last message was updated.
                // If it's a new message it will be marked as read in "else" branch, otherwise it
                // should be already marked as read.
                if (scrolledBottom() && justUpdated(lastMessage)) {
                    int newPosition = adapter.getItemCount() - 1;
                    logger.logI("just update last message");

                    postDelayed(() -> layoutManager.scrollToPosition(newPosition), 200);

                }
            }
        }
        int newSize = adapter.getItemCount();
        int sizeGrewBy = newSize - oldSize;

        if (!listItem.getHasNewMessages()) {
            // we only touch scroll for new messages, we ignore
            // read
            // typing
            // message updates
            logger.logI(String.format("no Scroll no new message"));
            return;
        }

        if (oldSize == 0 && newSize != 0) {
            int newPosition = adapter.getItemCount() - 1;
            layoutManager.scrollToPosition(newPosition);
            logger.logI(String.format("Scroll: First load scrolling down to bottom %d", newPosition));
            lastMessageReadHandler.invoke();
        } else if (listItem.getLoadingMore()) {
            // the load more behaviour is different, scroll positions starts out at 0
            // to stay at the relative 0 we should go to 0 + size of new messages...

            int newPosition;// = oldPosition + sizeGrewBy;
            newPosition = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition() + sizeGrewBy;
            layoutManager.scrollToPosition(newPosition);
        } else {
            if (newSize == 0) return;
            // regular new message behaviour
            // we scroll down all the way, unless you've scrolled up
            // if you've scrolled up we set a variable on the viewmodel that there are new messages
            int newPosition = adapter.getItemCount() - 1;
            int layoutSize = layoutManager.getItemCount();
            logger.logI(String.format("Scroll: Moving down to %d, layout has %d elements", newPosition, layoutSize));

            if (hasScrolledUp) {
                // always scroll to bottom when current user posts a message
                if (entities.size() > 1 && entities.get(entities.size() - 1).isMine()) {
                    layoutManager.scrollToPosition(newPosition);
                }
            } else {
                layoutManager.scrollToPosition(newPosition);
            }
            // we want to mark read if there is a new message
            // and this view is currently being displayed...
            // we can't always run it since read and typing events also influence this list..
            //viewModel.markLastMessageRead(); // TODO this is event
            lastMessageReadHandler.invoke();
        }
    }

    public MessageListViewStyle getStyle() {
        return style;
    }

    private boolean scrolledBottom() {
        int itemCount = adapter.getItemCount() - 2;
        return lVPosition >= itemCount;
    }

    private boolean justUpdated(Message message) {
        if (message.getUpdatedAt() == null) return false;
        Date now = new Date();
        long passedTime = now.getTime() - message.getUpdatedAt().getTime();
        return message.getUpdatedAt() != null
                && passedTime < 3000;
    }

    /**
     * Sets the message click listener to be used by MessageListView.
     *
     * @param messageClickListener The listener to use. If null, the default will be used instead.
     */
    public void setMessageClickListener(@Nullable MessageClickListener messageClickListener) {
        if (messageClickListener == null) {
            messageClickListener = DEFAULT_MESSAGE_CLICK_LISTENER;
        }
        listenerContainer.setMessageClickListener(messageClickListener);
    }

    /**
     * Sets the message long click listener to be used by MessageListView.
     *
     * @param messageLongClickListener The listener to use. If null, the default will be used instead.
     */
    public void setMessageLongClickListener(@Nullable MessageLongClickListener messageLongClickListener) {
        if (messageLongClickListener == null) {
            messageLongClickListener = DEFAULT_MESSAGE_LONG_CLICK_LISTENER;
        }
        listenerContainer.setMessageLongClickListener(messageLongClickListener);
    }

    /**
     * Sets the attachment click listener to be used by MessageListView.
     *
     * @param attachmentClickListener The listener to use. If null, the default will be used instead.
     */
    public void setAttachmentClickListener(@Nullable AttachmentClickListener attachmentClickListener) {
        if (attachmentClickListener == null) {
            attachmentClickListener = DEFAULT_ATTACHMENT_CLICK_LISTENER;
        }
        listenerContainer.setAttachmentClickListener(attachmentClickListener);
    }

    /**
     * Sets the reaction view click listener to be used by MessageListView.
     *
     * @param reactionViewClickListener The listener to use. If null, the default will be used instead.
     */
    public void setReactionViewClickListener(@Nullable ReactionViewClickListener reactionViewClickListener) {
        if (reactionViewClickListener == null) {
            reactionViewClickListener = DEFAULT_REACTION_VIEW_CLICK_LISTENER;
        }
        listenerContainer.setReactionViewClickListener(reactionViewClickListener);
    }

    /**
     * Sets the user click listener to be used by MessageListView.
     *
     * @param userClickListener The listener to use. If null, the default will be used instead.
     */
    public void setUserClickListener(@Nullable UserClickListener userClickListener) {
        if (userClickListener == null) {
            userClickListener = DEFAULT_USER_CLICK_LISTENER;
        }
        listenerContainer.setUserClickListener(userClickListener);
    }

    /**
     * Sets the read state click listener to be used by MessageListView.
     *
     * @param readStateClickListener The listener to use. If null, the default will be used instead.
     */
    public void setReadStateClickListener(@Nullable ReadStateClickListener readStateClickListener) {
        if (readStateClickListener == null) {
            readStateClickListener = DEFAULT_READ_STATE_CLICK_LISTENER;
        }
        listenerContainer.setReadStateClickListener(readStateClickListener);
    }

    public void setEndRegionReachedHandler(Function0<Unit> endRegionReachedHandler) {
        this.endRegionReachedHandler = endRegionReachedHandler;
    }

    public void setLastMessageReadHandler(Function0<Unit> lastMessageReadHandler) {
        this.lastMessageReadHandler = lastMessageReadHandler;
    }

    public void setOnMessageEditHandler(Function1<Message, Unit> onMessageEditHandler) {
        this.onMessageEditHandler = onMessageEditHandler;
    }

    public void setOnMessageDeleteHandler(Function1<Message, Unit> onMessageDeleteHandler) {
        this.onMessageDeleteHandler = onMessageDeleteHandler;
    }

    public void setOnStartThreadHandler(Function1<Message, Unit> onStartThreadHandler) {
        this.onStartThreadHandler = onStartThreadHandler;
    }

    public void setOnMessageFlagHandler(Function1<Message, Unit> onMessageFlagHandler) {
        this.onMessageFlagHandler = onMessageFlagHandler;
    }

    public void setOnSendGiphyHandler(Function2<Message, GiphyAction, Unit> onSendGiphyHandler) {
        this.onSendGiphyHandler = onSendGiphyHandler;
    }

    public void setOnStartThreadListener(Function1<Message, Unit> onStartThreadListener) {
        this.onStartThreadListener = onStartThreadListener;
    }

    public interface HeaderAvatarGroupClickListener {
        void onHeaderAvatarGroupClick(Channel channel);
    }

    public interface HeaderOptionsClickListener {
        void onHeaderOptionsClick(Channel channel);
    }

    public interface MessageClickListener {
        void onMessageClick(Message message, int position);
    }

    public interface MessageLongClickListener {
        void onMessageLongClick(Message message);
    }

    public interface AttachmentClickListener {
        void onAttachmentClick(Message message, Attachment attachment);
    }

    public interface GiphySendListener {
        void onGiphySend(Message message, GiphyAction action);
    }

    public interface UserClickListener {
        void onUserClick(User user);
    }

    public interface ReadStateClickListener {
        void onReadStateClick(List<ChannelUserRead> reads);
    }

    public interface ReactionViewClickListener {
        void onReactionViewClick(Message message);
    }

    public interface BubbleHelper {
        Drawable getDrawableForMessage(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions);

        Drawable getDrawableForAttachment(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions, Attachment attachment);

        Drawable getDrawableForAttachmentDescription(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions);
    }
}