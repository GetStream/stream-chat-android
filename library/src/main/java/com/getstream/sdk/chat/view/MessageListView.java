package com.getstream.sdk.chat.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.DefaultBubbleHelper;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.AttachmentViewHolderFactory;
import com.getstream.sdk.chat.adapter.ListenerContainer;
import com.getstream.sdk.chat.adapter.ListenerContainerImpl;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.enums.GiphyAction;
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination;
import com.getstream.sdk.chat.utils.StartStopBuffer;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.dialog.MessageMoreActionDialog;
import com.getstream.sdk.chat.view.dialog.ReadUsersDialog;
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper;

import org.jetbrains.annotations.NotNull;

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
public class MessageListView extends ConstraintLayout {
    //    private int firstVisible;
    private static int fVPosition, lVPosition;
    protected MessageListViewStyle style;

    private RecyclerView messagesRV;
    private ConstraintLayout unseenBottomBtn;
    boolean unseenButtonEnabled;
    private TextView newMessagesTextTV;
    private int lastViewedPosition = 0;
    private String newMessagesTextSingle;
    private String newMessagesTextPlural;
    private NewMessagesBehaviour newMessagesBehaviour;
    private ScrollButtonBehaviour scrollButtonBehaviour;
    private StartStopBuffer<MessageListItemWrapper> buffer;

    private int unseenItems = 0;

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
    private Function1<Message, Unit> onMessageRetryHandler = (Message m) -> {
        throw new IllegalStateException("onMessageRetryHandler must be set.");
    };
    private Channel channel;
    private User currentUser;
    /**
     * If you are allowed to scroll up or not
     */
    boolean lockScrollUp = true;

    private TaggedLogger logger = ChatLogger.Companion.get("MessageListView");

    private final MessageClickListener DEFAULT_MESSAGE_CLICK_LISTENER = (message) -> {
        if (message.getReplyCount() > 0) {
            onStartThreadHandler.invoke(message);
            onStartThreadListener.invoke(message);
        }
    };

    private final MessageLongClickListener DEFAULT_MESSAGE_LONG_CLICK_LISTENER = message ->
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

    private final MessageRetryListener DEFAULT_MESSAGE_RETRY_LISTENER = (message) -> {
        onMessageRetryHandler.invoke(message);
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
    private final ReadStateClickListener DEFAULT_READ_STATE_CLICK_LISTENER = reads ->
            new ReadUsersDialog(getContext())
                    .setReads(reads)
                    .setStyle(style)
                    .show();
    private final GiphySendListener DEFAULT_GIPHY_SEND_LISTENER = (message, action) ->
            onSendGiphyHandler.invoke(message, action);

    private final ListenerContainer listenerContainer = new ListenerContainerImpl(
            DEFAULT_MESSAGE_CLICK_LISTENER,
            DEFAULT_MESSAGE_LONG_CLICK_LISTENER,
            DEFAULT_MESSAGE_RETRY_LISTENER,
            DEFAULT_ATTACHMENT_CLICK_LISTENER,
            DEFAULT_REACTION_VIEW_CLICK_LISTENER,
            DEFAULT_USER_CLICK_LISTENER,
            DEFAULT_READ_STATE_CLICK_LISTENER,
            DEFAULT_GIPHY_SEND_LISTENER
    );

    private BubbleHelper bubbleHelper;
    private AttachmentViewHolderFactory attachmentViewHolderFactory;
    private MessageViewHolderFactory messageViewHolderFactory;

    // region Constructor
    public MessageListView(Context context) {
        super(context);
        init(context, null);
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.parseAttr(context, attrs);
        init(context, attrs);
    }

    public MessageListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.parseAttr(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attr) {
        LayoutInflater.from(context).inflate(R.layout.stream_message_list_view, this, true);

        initRecyclerView();
        initUnseenMessagesButton();
        initUnseenMessagesView();

        if (attr != null) {
            configureAttributes(attr);
        }

        initScrollButtonBehaviour();

        hasScrolledUp = false;

        buffer = new StartStopBuffer<>();

        buffer.subscribe((wrapper) -> {
            handleNewWrapper(wrapper);
            return Unit.INSTANCE;
        });
    }

    // region Init
    private void initScrollButtonBehaviour() {
        scrollButtonBehaviour =
                new DefaultScrollButtonBehaviour(
                        unseenBottomBtn,
                        newMessagesTextTV,
                        newMessagesTextSingle,
                        newMessagesTextPlural,
                        unseenButtonEnabled
                );
    }

    private void initRecyclerView() {
        messagesRV = findViewById(R.id.chatMessagesRV);

        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);

        messagesRV.setLayoutManager(layoutManager);
        messagesRV.setHasFixedSize(true);
        messagesRV.setItemViewCacheSize(20);
    }

    private void initUnseenMessagesButton() {
        unseenBottomBtn = findViewById(R.id.scrollBottomBtn);
        unseenBottomBtn.setOnClickListener(view ->
                messagesRV.smoothScrollToPosition(lastPosition())
        );
    }

    private void initUnseenMessagesView() {
        newMessagesTextTV = findViewById(R.id.newMessagesTV);
        newMessagesTextTV.setVisibility(View.GONE);
    }

    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new MessageListViewStyle(context, attrs);
    }

    //private methods
    private void configureAttributes(AttributeSet attributeSet) {
        TypedArray tArray = getContext()
                .obtainStyledAttributes(attributeSet, R.styleable.MessageListView);

        int backgroundRes = tArray.getResourceId(
                R.styleable.MessageListView_streamButtonBackground,
                R.drawable.stream_shape_round);

        unseenButtonEnabled = tArray.getBoolean(
                R.styleable.MessageListView_streamDefaultButtonEnabled, true);

        if (unseenButtonEnabled) {
            unseenBottomBtn.setBackgroundResource(backgroundRes);
        } else {
            unseenBottomBtn.setVisibility(View.GONE);
        }

        newMessagesTextSingle =
                tArray.getString(R.styleable.MessageListView_streamNewMessagesTextSingle);
        newMessagesTextPlural =
                tArray.getString(R.styleable.MessageListView_streamNewMessagesTextPlural);

        newMessagesBehaviour = NewMessagesBehaviour.parseValue(
                tArray.getInt(
                        R.styleable.MessageListView_streamNewMessagesBehaviour,
                        NewMessagesBehaviour.COUNT_UPDATE.value)
        );

        int arrowIconRes = tArray.getResourceId(
                R.styleable.MessageListView_streamButtonIcon,
                R.drawable.stream_bottom_arrow);

        ImageView scrollButtonArrow = findViewById(R.id.scrollIconIV);
        scrollButtonArrow.setImageResource(arrowIconRes);

        tArray.recycle();
    }

    private int lastPosition() {
        return adapter.getItemCount() - 1;
    }

    private void setMessageListItemAdapter(@NonNull MessageListItemAdapter adapter) {
        messagesRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                if (layoutManager != null) {
                    int currentFirstVisible = layoutManager.findFirstVisibleItemPosition();
                    int currentLastVisible = layoutManager.findLastVisibleItemPosition();
                    if (currentFirstVisible < fVPosition && currentFirstVisible == 0) {
                        endRegionReachedHandler.invoke();
                    }

                    hasScrolledUp = currentLastVisible < lastPosition();
                    lVPosition = currentLastVisible;
                    fVPosition = currentFirstVisible;

                    lastViewedPosition = Math.max(currentLastVisible, lastViewedPosition);

                    unseenItems = adapter.getItemCount() - 1 - lastViewedPosition;
                    scrollButtonBehaviour.onUnreadMessageCountChanged(unseenItems);

                    if (hasScrolledUp) {
                        scrollButtonBehaviour.userScrolledUp();
                    } else {
                        scrollButtonBehaviour.userScrolledToTheBottom();
                    }
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

        messagesRV.setAdapter(adapter);
    }

    public void init(Channel channel, User currentUser) {
        this.currentUser = currentUser;
        this.channel = channel;
        initAdapter();
    }

    private void initAdapter() {
        // Create default AttachmentViewHolderFactory if needed
        if (attachmentViewHolderFactory == null) {
            attachmentViewHolderFactory = new AttachmentViewHolderFactory();
        }
        // Create default ViewHolderFactory if needed
        if (messageViewHolderFactory == null) {
            messageViewHolderFactory = new MessageViewHolderFactory();
        }
        // Create default BubbleHelper if needed
        if (bubbleHelper == null) {
            bubbleHelper = DefaultBubbleHelper.initDefaultBubbleHelper(style, getContext());
        }

        // Inject Attachment factory
        attachmentViewHolderFactory.setListenerContainerInternal(listenerContainer);
        attachmentViewHolderFactory.setBubbleHelperInternal(bubbleHelper);

        // Inject Message factory
        messageViewHolderFactory.setListenerContainerInternal(listenerContainer);
        messageViewHolderFactory.setAttachmentViewHolderFactoryInternal(attachmentViewHolderFactory);
        messageViewHolderFactory.setBubbleHelperInternal(bubbleHelper);

        adapter = new MessageListItemAdapter(channel, messageViewHolderFactory, style);
        adapter.setHasStableIds(true);

        this.setMessageListItemAdapter(adapter);
    }

    public void setScrollButtonBehaviour(ScrollButtonBehaviour scrollButtonBehaviour) {
        this.scrollButtonBehaviour = scrollButtonBehaviour;
    }

    public void setNewMessagesBehaviour(NewMessagesBehaviour newMessagesBehaviour) {
        this.newMessagesBehaviour = newMessagesBehaviour;
    }

    public void setScrollButtonBackgroundResource(@DrawableRes int backgroundRes) {
        unseenBottomBtn.setBackgroundResource(backgroundRes);
    }

    public void setScrollButtonBackground(Drawable drawable) {
        unseenBottomBtn.setBackground(drawable);
    }

    public void setScrollButtonIconResource(@DrawableRes int backgroundRes) {
        ImageView icon = findViewById(R.id.scrollIconIV);
        icon.setImageResource(backgroundRes);
    }

    public void setScrollButtonIcon(Drawable drawable) {
        ImageView icon = findViewById(R.id.scrollIconIV);
        icon.setImageDrawable(drawable);
    }

    public void setAttachmentViewHolderFactory(AttachmentViewHolderFactory attachmentViewHolderFactory) {
        if (adapter != null) {
            throw new IllegalStateException("Adapter was already initialized, please set AttachmentViewHolderFactory first");
        }
        this.attachmentViewHolderFactory = attachmentViewHolderFactory;
    }

    public void setMessageViewHolderFactory(MessageViewHolderFactory messageViewHolderFactory) {
        if (adapter != null) {
            throw new IllegalStateException("Adapter was already initialized, please set MessageViewHolderFactory first");
        }
        this.messageViewHolderFactory = messageViewHolderFactory;
    }

    /**
     * Use the more explicit setMessageViewHolderFactory method instead.
     */
    @Deprecated
    public void setViewHolderFactory(MessageViewHolderFactory messageViewHolderFactory) {
        if (adapter != null) {
            throw new IllegalStateException("Adapter was already initialized, please set MessageViewHolderFactory first");
        }
        this.messageViewHolderFactory = messageViewHolderFactory;
    }

    public void setBubbleHelper(BubbleHelper bubbleHelper) {
        if (adapter != null) {
            throw new IllegalStateException("Adapter was already initialized, please set BubbleHelper first");
        }
        this.bubbleHelper = bubbleHelper;
    }

    public void displayNewMessage(MessageListItemWrapper listItem) {
        buffer.enqueueData(listItem);
    }

    private void handleNewWrapper(MessageListItemWrapper listItem) {
        buffer.hold();
        List<MessageListItem> entities = listItem.getListEntities();

        // Adapter initialization for channel and thread swapping
        boolean backFromThread = adapter.isThread() && listItem.isThread();

        if (adapter.isThread() != listItem.isThread()) {
            adapter.setThread(listItem.isThread());
        }

        adapter.submitList(
                entities,
                () -> continueMessageAdd(backFromThread, listItem, entities, adapter.getItemCount())
        );
    }

    private void continueMessageAdd(
            boolean backFromThread,
            MessageListItemWrapper listItem,
            List<MessageListItem> entities,
            int oldSize
    ) {
        int newSize = adapter.getItemCount();
        int sizeGrewBy = newSize - oldSize;

        // Scroll to origin position on return from thread
        if (backFromThread) {
            layoutManager.scrollToPosition(this.threadParentPosition);
            lastMessageReadHandler.invoke();
            return;
        }

        // Scroll to bottom position for typing indicator
        if (listItem.isTyping() && scrolledBottom(sizeGrewBy + 2)) {
            int newPosition = adapter.getItemCount() - 1;
            layoutManager.scrollToPosition(newPosition);
            return;
        }

        if (!listItem.getHasNewMessages()) {
            // we only touch scroll for new messages, we ignore
            // read
            // typing
            // message updates
            logger.logI("no Scroll no new message");
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
            newPosition = ((LinearLayoutManager) messagesRV.getLayoutManager()).findLastCompletelyVisibleItemPosition() + sizeGrewBy;
            layoutManager.scrollToPosition(newPosition);
        } else {
            if (newSize == 0) return;
            // regular new message behaviour
            // we scroll down all the way, unless you've scrolled up
            // if you've scrolled up we set a variable on the viewmodel that there are new messages
            int newPosition = adapter.getItemCount() - 1;
            int layoutSize = layoutManager.getItemCount();
            logger.logI(String.format("Scroll: Moving down to %d, layout has %d elements", newPosition, layoutSize));
            // Scroll to bottom when the user wrote the message.
            if (entities.size() >= 1 && entities.get(entities.size() - 1).isMine() ||
                    !hasScrolledUp ||
                    newMessagesBehaviour == NewMessagesBehaviour.SCROLL_TO_BOTTOM) {
                layoutManager.scrollToPosition(adapter.getItemCount() - 1);
            } else {
                unseenItems = newSize - 1 - lastViewedPosition;
                scrollButtonBehaviour.onUnreadMessageCountChanged(unseenItems);
            }
            // we want to mark read if there is a new message
            // and this view is currently being displayed...
            // we can't always run it since read and typing events also influence this list..
            //viewModel.markLastMessageRead(); // TODO this is event
            lastMessageReadHandler.invoke();
        }

        buffer.active();
    }

    public MessageListViewStyle getStyle() {
        return style;
    }

    private boolean scrolledBottom(int delta) {
        return lVPosition + delta >= lastPosition();
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
     * Sets the message retry listener to be used by MessageListView.
     *
     * @param messageRetryListener The listener to use. If null, the default will be used instead.
     */
    public void setMessageRetryListener(@Nullable MessageRetryListener messageRetryListener) {
        if (messageRetryListener == null) {
            messageRetryListener = DEFAULT_MESSAGE_RETRY_LISTENER;
        }
        listenerContainer.setMessageRetryListener(messageRetryListener);
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

    public void setOnMessageRetryHandler(Function1<Message, Unit> onMessageRetryHandler) {
        this.onMessageRetryHandler = onMessageRetryHandler;
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
        void onMessageClick(Message message);
    }

    public interface MessageRetryListener {
        void onRetryMessage(Message message);
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

    public enum NewMessagesBehaviour {
        SCROLL_TO_BOTTOM(0), COUNT_UPDATE(1);

        private final int value;

        NewMessagesBehaviour(int value) {
            this.value = value;
        }

        public final int getValue() {
            return value;
        }

        public static NewMessagesBehaviour parseValue(int value) {
            if (value == SCROLL_TO_BOTTOM.getValue())
                return SCROLL_TO_BOTTOM;
            if (value == COUNT_UPDATE.getValue())
                return COUNT_UPDATE;
            throw new IllegalArgumentException(
                    "Unknown behaviour type. It must be either SCROLL_TO_BOTTOM (int 0) or COUNT_UPDATE (int 1)"
            );
        }
    }

    public interface ScrollButtonBehaviour {

        void userScrolledUp();

        void userScrolledToTheBottom();

        void onUnreadMessageCountChanged(int count);
    }

    static class DefaultScrollButtonBehaviour implements ScrollButtonBehaviour {

        private ViewGroup unseenBottomBtn;
        private TextView newMessagesTextTV;
        private String newMessagesTextSingle;
        private String newMessagesTextPlural;
        private boolean isButtonEnabled;

        private DefaultScrollButtonBehaviour(
                ViewGroup unseenBottomBtn,
                TextView newMessagesTextTV,
                String newMessagesTextSingle,
                String newMessagesTextPlural,
                boolean isButtonEnabled
        ) {
            this.unseenBottomBtn = unseenBottomBtn;
            this.newMessagesTextTV = newMessagesTextTV;
            this.newMessagesTextSingle = newMessagesTextSingle;
            this.newMessagesTextPlural = newMessagesTextPlural;
            this.isButtonEnabled = isButtonEnabled;
        }

        public DefaultScrollButtonBehaviour(
                ViewGroup unseenBottomBtn,
                TextView newMessagesTextTV,
                String newMessagesTextSingle,
                String newMessagesTextPlural
        ) {
            this.unseenBottomBtn = unseenBottomBtn;
            this.newMessagesTextTV = newMessagesTextTV;
            this.newMessagesTextSingle = newMessagesTextSingle;
            this.newMessagesTextPlural = newMessagesTextPlural;
            isButtonEnabled = true;
        }

        @Override
        public void userScrolledUp() {
            if (!unseenBottomBtn.isShown() && isButtonEnabled) {
                unseenBottomBtn.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void userScrolledToTheBottom() {
            if (unseenBottomBtn.isShown()) {
                unseenBottomBtn.setVisibility(View.GONE);
            }
        }

        @Override
        public void onUnreadMessageCountChanged(int count) {
            if (count <= 0) {
                newMessagesTextTV.setVisibility(View.GONE);
            } else {
                newMessagesTextTV.setVisibility(View.VISIBLE);
                newMessagesTextTV.setText(parseNewMessagesText(count));
            }
        }

        private String parseNewMessagesText(int unseenItems) {
            if (unseenItems == 1) {
                if (newMessagesTextSingle != null) {
                    return String.format(newMessagesTextSingle, unseenItems);
                } else {
                    return String.valueOf(unseenItems);
                }
            } else {
                if (newMessagesTextPlural != null) {
                    return String.format(newMessagesTextPlural, unseenItems);
                } else {
                    return String.valueOf(unseenItems);
                }
            }
        }
    }
}
