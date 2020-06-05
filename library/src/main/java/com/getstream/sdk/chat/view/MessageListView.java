package com.getstream.sdk.chat.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.DefaultBubbleHelper;
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
    private MessageListItemAdapter adapter;
    private LinearLayoutManager layoutManager;
    private MessageViewHolderFactory viewHolderFactory;
    private MessageClickListener messageClickListener;
    private MessageLongClickListener messageLongClickListener;
    private AttachmentClickListener attachmentClickListener;
    private ReactionViewClickListener reactionViewClickListener;
    private UserClickListener userClickListener;
    private ReadStateClickListener readStateClickListener;
    private boolean hasScrolledUp;
    private BubbleHelper bubbleHelper;
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
    private Function1<Message, Unit> onMessageFlagHandler = (Message m) -> {
        throw new IllegalStateException("onMessageFlagHandler must be set.");
    };
    private Function2<Message, GiphyAction, Unit> onSendGiphyHandler = (Message m, GiphyAction a) -> {
        throw new IllegalStateException("onMessageFlagHandler must be set.");
    };
    private Channel channel;
    private User currentUser;
    /**
     * If you are allowed to scroll up or not
     */
    boolean lockScrollUp = true;

    private TaggedLogger logger = ChatLogger.Companion.get("MessageListView");

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
        setBubbleHelper(DefaultBubbleHelper.initDefaultBubbleHelper(style, context));
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
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    private void setAdapterWithStyle(MessageListItemAdapter adapter) {
        adapter.setStyle(style);
        adapter.setGiphySendListener((Message message, GiphyAction action) -> onSendGiphyHandler.invoke(message, action));
        setMessageClickListener(messageClickListener);
        setMessageLongClickListener(messageLongClickListener);
        setAttachmentClickListener(attachmentClickListener);
        setReactionViewClickListener(reactionViewClickListener);
        setUserClickListener(userClickListener);
        setReadStateClickListener(readStateClickListener);
        setMessageLongClickListener(messageLongClickListener);
        adapter.setChannel(channel);

        this.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager != null) {

                    int currentFirstVisible = layoutManager.findFirstVisibleItemPosition();
                    int currentLastVisible = layoutManager.findLastVisibleItemPosition();

                    if (currentFirstVisible < fVPosition && currentFirstVisible == 0)
                        endRegionReachedHandler.invoke();

                    hasScrolledUp = currentLastVisible <= (adapter.getItemCount() - 3);
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

    private void initAdapter() {
        // Setup a default adapter and pass the style
        adapter = new MessageListItemAdapter(getContext());
        adapter.setHasStableIds(true);

        if (viewHolderFactory != null) {
            adapter.setFactory(viewHolderFactory);
        }

        if (bubbleHelper != null) {
            adapter.setBubbleHelper(bubbleHelper);
        }

        this.setAdapterWithStyle(adapter);
    }

    public void displayNewMessage(MessageListItemWrapper listItem) {
        List<MessageListItem> entities = listItem.getListEntities();

        // Adapter initialization for channel and thread swapping
        boolean backFromThread = false;
        if (adapter.isThread() != listItem.isThread()) {
            adapter.setThread(listItem.isThread());
            backFromThread = !listItem.isThread();
        }

        adapter.replaceEntities(entities);

        // Scroll to origin position on return from thread
        if (backFromThread) {
            layoutManager.scrollToPosition(this.threadParentPosition);
            //viewModel.markLastMessageRead(); // TODO this is event
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
            Message lastMessage = entities.get(entities.size() - 1).getMessage();
            if (lastMessage != null
                    && scrolledBottom()
                    && justUpdated(lastMessage)) {
                int newPosition = adapter.getItemCount() - 1;
                logger.logI(String.format("just update last message"));

                postDelayed(() -> layoutManager.scrollToPosition(newPosition), 200);

                return;
            }
        }

        int oldSize = adapter.getItemCount();
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

    public void setViewHolderFactory(MessageViewHolderFactory factory) {
        this.viewHolderFactory = factory;
        if (adapter != null) {
            adapter.setFactory(factory);
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

    public void setMessageClickListener(MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;

        if (adapter == null) return;

        if (this.messageClickListener != null) {
            adapter.setMessageClickListener(this.messageClickListener);
        } else {
            adapter.setMessageClickListener((message, position) -> {
                if (message.getReplyCount() > 0) {
                    // viewModel.setActiveThread(message); // TODO this is event
                    onStartThreadHandler.invoke(message);
                } else {
                    //viewModel.sendMessage(message);
                }
            });
        }
    }

    public void setMessageLongClickListener(MessageLongClickListener messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;

        if (adapter == null) return;

        if (this.messageLongClickListener != null) {
            adapter.setMessageLongClickListener(this.messageLongClickListener);
        } else {
            adapter.setMessageLongClickListener(message ->
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
                    ).show());
        }
    }

    public void setAttachmentClickListener(AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;

        if (adapter == null) return;

        if (this.attachmentClickListener != null) {
            adapter.setAttachmentClickListener(this.attachmentClickListener);
        } else {
            adapter.setAttachmentClickListener(this::showAttachment);
        }
    }

    public void showAttachment(Message message, Attachment attachment) {
        Chat.getInstance().getNavigator().navigate(new AttachmentDestination(message, attachment, getContext()));
    }

    public void setReactionViewClickListener(ReactionViewClickListener l) {
        this.reactionViewClickListener = l;
        if (adapter == null) return;

        if (this.reactionViewClickListener != null) {
            adapter.setReactionViewClickListener(this.reactionViewClickListener);
        } else {
            adapter.setReactionViewClickListener(message -> {
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
            });
        }
    }

    public void setUserClickListener(UserClickListener userClickListener) {
        this.userClickListener = userClickListener;

        if (adapter == null) return;

        if (this.userClickListener != null) {
            adapter.setUserClickListener(this.userClickListener);
        }
    }

    public void setReadStateClickListener(ReadStateClickListener readStateClickListener) {
        this.readStateClickListener = readStateClickListener;
        if (adapter == null) return;

        if (this.readStateClickListener != null) {
            adapter.setReadStateClickListener(this.readStateClickListener);
        } else {
            adapter.setReadStateClickListener(reads -> {
                new ReadUsersDialog(getContext())
                        .setReads(reads)
                        .setStyle(style)
                        .show();
            });
        }
    }

    public void setBubbleHelper(BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
        if (adapter != null) {
            adapter.setBubbleHelper(bubbleHelper);
        }
    }

    public void init(Channel channel, User currentUser) {
        this.currentUser = currentUser;
        this.channel = channel;
        initAdapter();
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