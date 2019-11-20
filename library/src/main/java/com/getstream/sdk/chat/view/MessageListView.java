package com.getstream.sdk.chat.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.getstream.sdk.chat.DefaultBubbleHelper;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.enums.GiphyAction;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.frescoimageviewer.ImageViewer;
import com.getstream.sdk.chat.view.Dialog.MessageMoreActionDialog;
import com.getstream.sdk.chat.view.Dialog.ReadUsersDialog;
import com.getstream.sdk.chat.view.activity.AttachmentActivity;
import com.getstream.sdk.chat.view.activity.AttachmentDocumentActivity;
import com.getstream.sdk.chat.view.activity.AttachmentMediaActivity;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.Date;
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
    //    private int firstVisible;
    private static int fVPosition, lVPosition;
    final String TAG = MessageListView.class.getSimpleName();
    protected MessageListViewStyle style;
    private MessageListItemAdapter adapter;
    private LinearLayoutManager layoutManager;
    // our connection to the channel scope
    private ChannelViewModel viewModel;
    private MessageViewHolderFactory viewHolderFactory;
    private MessageClickListener messageClickListener;
    private MessageLongClickListener messageLongClickListener;
    private AttachmentClickListener attachmentClickListener;
    private GiphySendListener giphySendListener;
    private ReactionViewClickListener reactionViewClickListener;
    private UserClickListener userClickListener;
    private ReadStateClickListener readStateClickListener;
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
        setBubbleHelper(DefaultBubbleHelper.initDefaultBubbleHelper(style, context));
        setHasFixedSize(true);
        setItemViewCacheSize(20);
    }
    // endregion

    // region Init
    private void parseAttr(Context context, @Nullable AttributeSet attrs) {
        // parse the attributes
        style = new MessageListViewStyle(context, attrs);
    }

    private void init() {
        try {
            Fresco.initialize(getContext());
        } catch (Exception e) {
        }
        giphySendListener = viewModel::sendGiphy;
    }

    // set the adapter and apply the style.
    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("Use setAdapterWithStyle instead please");
    }

    public void setAdapterWithStyle(MessageListItemAdapter adapter) {

        adapter.setStyle(style);
        adapter.setGiphySendListener(giphySendListener);
        setMessageClickListener(messageClickListener);
        setMessageLongClickListener(messageLongClickListener);
        setAttachmentClickListener(attachmentClickListener);
        setReactionViewClickListener(reactionViewClickListener);
        setUserClickListener(userClickListener);
        setReadStateClickListener(readStateClickListener);
        setMessageLongClickListener(messageLongClickListener);
        adapter.setChannelState(getChannel().getChannelState());

        this.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager != null) {

                    int currentFirstVisible = layoutManager.findFirstVisibleItemPosition();
                    int currentLastVisible = layoutManager.findLastVisibleItemPosition();

                    if (currentFirstVisible < fVPosition && currentFirstVisible == 0)
                        viewModel.loadMore();

                    hasScrolledUp = currentLastVisible <= (adapter.getItemCount() - 3);
                    if (!hasScrolledUp) {
                        viewModel.setHasNewMessages(false);
                    }
                    viewModel.setMessageListScrollUp(currentLastVisible + 1 < lVPosition);
                    fVPosition = currentFirstVisible;
                    lVPosition = currentLastVisible;
                    viewModel.setThreadParentPosition(lVPosition);
                }
            }
        });
        super.setAdapter(adapter);
    }

    public void setViewModel(ChannelViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;
        init();


        // Setup a default adapter and pass the style
        adapter = new MessageListItemAdapter(getContext());
        adapter.setHasStableIds(true);

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

            // Adapter initialization for channel and thread swapping
            boolean backFromThread = false;
            if (adapter.isThread() != messageListItemWrapper.isThread()) {
                adapter.setThread(messageListItemWrapper.isThread());
                backFromThread = !messageListItemWrapper.isThread();
            }

            adapter.replaceEntities(entities);

            // Scroll to origin position on return from thread
            if (backFromThread) {
                layoutManager.scrollToPosition(viewModel.getThreadParentPosition());
                viewModel.markLastMessageRead();
                return;
            }

            // Scroll to bottom position for typing indicator
            if (messageListItemWrapper.isTyping() && scrolledBottom()) {
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
                    layoutManager.scrollToPosition(newPosition);
                    Log.i(TAG, String.format("just update last message"));
                    return;
                }
            }

            int oldSize = adapter.getItemCount();
            int newSize = adapter.getItemCount();
            int sizeGrewBy = newSize - oldSize;

            if (!messageListItemWrapper.getHasNewMessages()) {
                // we only touch scroll for new messages, we ignore
                // read
                // typing
                // message updates
                Log.i(TAG, String.format("no Scroll no new message"));
                return;
            }

            if (oldSize == 0 && newSize != 0) {
                int newPosition = adapter.getItemCount() - 1;
                layoutManager.scrollToPosition(newPosition);
                Log.i(TAG, String.format("Scroll: First load scrolling down to bottom %d", newPosition));
            } else if (messageListItemWrapper.getLoadingMore()) {
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
                Log.i(TAG, String.format("Scroll: Moving down to %d, layout has %d elements", newPosition, layoutSize));

                if (hasScrolledUp) {
                    // always scroll to bottom when current user posts a message
                    if (entities.size() > 1 && entities.get(entities.size() - 1).isMine()) {
                        layoutManager.scrollToPosition(newPosition);
                    }
                    viewModel.setHasNewMessages(true);
                } else {
                    layoutManager.scrollToPosition(newPosition);
                    viewModel.setHasNewMessages(false);
                }
                // we want to mark read if there is a new message
                // and this view is currently being displayed...
                // we can't always run it since read and typing events also influence this list..
                viewModel.markLastMessageRead();
            }
        });

        viewModel.getThreadParentMessage().observe(lifecycleOwner, message -> {
        });

        this.setAdapterWithStyle(adapter);
    }

    public void setViewHolderFactory(MessageViewHolderFactory factory) {
        this.viewHolderFactory = factory;
        if (adapter != null) {
            adapter.setFactory(factory);
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

    private boolean scrolledBottom() {
        int itemCount = adapter.getItemCount() - 2;
        return lVPosition >= itemCount;
    }

    private boolean justUpdated(Message message) {
        if (message.getUpdatedAt() == null) return false;
        Date now = new Date();
        long passedTime = now.getTime() - message.getUpdatedAt().getTime();
        return message.getUpdatedAt() != null
                && passedTime < 2000;
    }
    // endregion

    // region Thread

    // endregion

    // region Listener
    public void setMessageClickListener(MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;

        if (adapter == null) return;

        if (this.messageClickListener != null) {
            adapter.setMessageClickListener(this.messageClickListener);
        } else {
            adapter.setMessageClickListener((message, position) -> {
                if (message.getSyncStatus() == Sync.LOCAL_FAILED) {
                    viewModel.sendMessage(message);
                } else if (message.getReplyCount() > 0) {
                    viewModel.setThreadParentMessage(message);
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
                new MessageMoreActionDialog(getContext())
                        .setChannelViewModel(viewModel)
                        .setMessage(message)
                        .setStyle(style)
                        .show()
            );
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

    public void setReactionViewClickListener(ReactionViewClickListener l) {
        this.reactionViewClickListener = l;
        if (adapter == null) return;

        if (this.reactionViewClickListener != null) {
            adapter.setReactionViewClickListener(this.reactionViewClickListener);
        } else {
            adapter.setReactionViewClickListener(message -> {
                Utils.hideSoftKeyboard((Activity) getContext());
                new MessageMoreActionDialog(getContext())
                        .setChannelViewModel(viewModel)
                        .setMessage(message)
                        .setStyle(style)
                        .show();
            });
        }
    }

    public void setUserClickListener(UserClickListener userClickListener) {
        this.userClickListener = userClickListener;

        if (adapter == null) return;

        if (this.userClickListener != null) {
            adapter.setUserClickListener(this.userClickListener);
        } else {

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
                        .setChannelViewModel(viewModel)
                        .setReads(reads)
                        .setStyle(style)
                        .show();
            });
        }
    }

    /*Used for disable mark read Message*/
    public void disableMarkRead(){
        viewModel.setEnableMarkRead(false);
    }

    /*Used for enable mark read Message*/
    public void enableMarkRead(){
        viewModel.setEnableMarkRead(true);
    }

    public void setBubbleHelper(BubbleHelper bubbleHelper) {
        this.bubbleHelper = bubbleHelper;
        if (adapter != null) {
            adapter.setBubbleHelper(bubbleHelper);
        }
    }

    // region View Attachment
    public void showAttachment(Message message, Attachment attachment) {
        String url = null;
        String type = null;
        if (attachment == null)
            return;

        switch (attachment.getType()) {
            case ModelType.attach_file:
                loadFile(attachment);
                return;
            case ModelType.attach_image:
                if (attachment.getOgURL() != null) {
                    url = attachment.getOgURL();
                    type = ModelType.attach_link;
                } else {
                    List<String> imageUrls = new ArrayList<>();
                    for (Attachment a : message.getAttachments()) {
                        if (!a.getType().equals(ModelType.attach_image) || TextUtils.isEmpty(a.getImageURL()))
                            continue;
                        imageUrls.add(a.getImageURL());
                    }
                    if (imageUrls.isEmpty()) {
                        Utils.showMessage(getContext(), "Invalid image(s)!");
                        return;
                    }

                    int position = message.getAttachments().indexOf(attachment);
                    if (position > imageUrls.size() - 1) position = 0;
                    new ImageViewer.Builder<>(getContext(), imageUrls)
                            .setStartPosition(position)
                            .show();
                    return;
                }
                break;
            case ModelType.attach_video:
                url = attachment.getAssetURL();
                break;
            case ModelType.attach_giphy:
                url = attachment.getThumbURL();
                break;
            case ModelType.attach_product:
                url = attachment.getUrl();
                break;
        }
        if (TextUtils.isEmpty(url)){
            Utils.showMessage(getContext(), getContext().getString(R.string.stream_attachment_invalid_url));
            return;
        }

        if (type == null) type = attachment.getType();
        Intent intent = new Intent(getContext(), AttachmentActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("url", url);
        getContext().startActivity(intent);
    }

    private void loadFile(Attachment attachment) {
        // Media
        if (attachment.getMime_type().contains("audio") ||
                attachment.getMime_type().contains("video")) {
            Intent intent = new Intent(getContext(), AttachmentMediaActivity.class);
            intent.putExtra("type", attachment.getMime_type());
            intent.putExtra("url", attachment.getAssetURL());
            getContext().startActivity(intent);
            return;
        }

        // Office
        if (attachment.getMime_type().equals("application/msword") ||
                attachment.getMime_type().equals(ModelType.attach_mime_txt) ||
                attachment.getMime_type().equals(ModelType.attach_mime_pdf) ||
                attachment.getMime_type().contains("application/vnd")) {

            Intent intent = new Intent(getContext(), AttachmentDocumentActivity.class);
            intent.putExtra("url", attachment.getAssetURL());
            getContext().startActivity(intent);
        }
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
    // endregion

}