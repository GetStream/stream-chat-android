package com.getstream.sdk.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.ChatMarkdown;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination;
import com.getstream.sdk.chat.utils.LlcMigrationUtils;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.AvatarView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;
import com.getstream.sdk.chat.view.ReadStateView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.utils.SyncStatus;
import top.defaults.drawabletoolbox.DrawableBuilder;

public class MessageListItemViewHolder extends BaseMessageListItemViewHolder<MessageListItem.MessageItem> {
    private static final DateFormat TIME_DATEFORMAT = new SimpleDateFormat("HH:mm");
    @DimenRes
    int avatarWidth;
    protected TextView tv_text;
    protected RecyclerView rv_reaction;
    protected AvatarView avatar;
    protected ImageView iv_tail;
    protected Space space_reaction_tail, space_header, space_same_user, space_reaction, space_attachment;
    protected TextView tv_username, tv_messagedate;
    // Delivered Indicator
    protected ReadStateView<MessageListViewStyle> read_state;
    protected ProgressBar pb_deliver;
    protected ImageView iv_deliver;
    protected AttachmentListView attachmentview;
    // Replay
    protected ImageView iv_reply;
    protected TextView tv_reply;

    protected RecyclerView.LayoutManager mLayoutManager;

    protected Channel channel;
    protected MessageListViewStyle style;
    protected MessageListView.BubbleHelper bubbleHelper;
    protected MessageViewHolderFactory viewHolderFactory;
    protected int position;
    protected Context context;
    protected Message message;
    protected MessageListItem.MessageItem messageListItem;

    @NonNull
    protected MessageListView.MessageClickListener messageClickListener;
    @NonNull
    protected MessageListView.MessageLongClickListener messageLongClickListener;
    @NonNull
    protected MessageListView.AttachmentClickListener attachmentClickListener;
    @NonNull
    protected MessageListView.ReactionViewClickListener reactionViewClickListener;
    @NonNull
    protected MessageListView.UserClickListener userClickListener;
    @NonNull
    protected MessageListView.ReadStateClickListener readStateClickListener;
    @NonNull
    protected MessageListView.GiphySendListener giphySendListener;

    protected List<MessageViewHolderFactory.Position> positions;

    protected ConstraintSet set;

    public MessageListItemViewHolder(
            int resId,
            ViewGroup viewGroup,
            @NonNull MessageListView.MessageClickListener messageClickListener,
            @NonNull MessageListView.MessageLongClickListener messageLongClickListener,
            @NonNull MessageListView.AttachmentClickListener attachmentClickListener,
            @NonNull MessageListView.ReactionViewClickListener reactionViewClickListener,
            @NonNull MessageListView.UserClickListener userClickListener,
            @NonNull MessageListView.ReadStateClickListener readStateClickListener,
            @NonNull MessageListView.GiphySendListener giphySendListener
    ) {
        super(resId, viewGroup);
        this.messageClickListener = messageClickListener;
        this.messageLongClickListener = messageLongClickListener;
        this.attachmentClickListener = attachmentClickListener;
        this.reactionViewClickListener = reactionViewClickListener;
        this.userClickListener = userClickListener;
        this.readStateClickListener = readStateClickListener;
        this.giphySendListener = giphySendListener;

        rv_reaction = itemView.findViewById(R.id.reactionsRecyclerView);
        iv_tail = itemView.findViewById(R.id.iv_tail);
        space_reaction_tail = itemView.findViewById(R.id.space_reaction_tail);

        tv_text = itemView.findViewById(R.id.tv_text);

        avatar = itemView.findViewById(R.id.avatar);

        iv_reply = itemView.findViewById(R.id.iv_reply);
        tv_reply = itemView.findViewById(R.id.tv_reply);

        tv_username = itemView.findViewById(R.id.tv_username);
        tv_messagedate = itemView.findViewById(R.id.tv_messagedate);

        space_header = itemView.findViewById(R.id.space_header);
        space_same_user = itemView.findViewById(R.id.space_same_user);
        space_reaction = itemView.findViewById(R.id.space_reaction);
        space_attachment = itemView.findViewById(R.id.space_attachment);

        attachmentview = itemView.findViewById(R.id.attachmentview);

        read_state = itemView.findViewById(R.id.read_state);
        pb_deliver = itemView.findViewById(R.id.pb_deliver);
        iv_deliver = itemView.findViewById(R.id.iv_deliver);

        mLayoutManager = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull Channel channel,
                     @NonNull MessageListItem.MessageItem messageListItem,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @NonNull MessageViewHolderFactory factory,
                     int position) {

        // set binding
        this.context = context;
        this.channel = channel;
        this.messageListItem = messageListItem;
        this.style = style;
        this.bubbleHelper = bubbleHelper;
        this.viewHolderFactory = factory;
        this.position = position;

        this.message = messageListItem.getMessage();
        this.positions = messageListItem.getPositions();
        this.set = new ConstraintSet();
        this.avatarWidth = style.getAvatarWidth();
        init();
    }

    // region Init
    protected void init() {
        // Configure UIs
        configMessageText();
        configAttachmentView();
        configReactionView();
        configReplyView();
        configDeliveredIndicator();
        configReadIndicator();
        // apply position related style tweaks
        configSpaces();
        configUserAvatar();
        configUserNameAndMessageDateStyle();
        // Configure Layout Params
        configMarginStartEnd();
        configParamsMessageText();
        configParamsUserAvatar();

        configParamsReactionSpace();
        configParamsReactionTail();
        configParamsReactionRecycleView();
        configParamsMessageDate();
        configParamsReply();
        configParamsReadIndicator();
    }
    // endregion

    // region Config
    // extra spacing
    protected void configSpaces() {
        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
            // TOP
            space_header.setVisibility(View.VISIBLE);
            space_same_user.setVisibility(View.GONE);
        } else {
            space_header.setVisibility(View.GONE);
            space_same_user.setVisibility(View.VISIBLE);
        }
        // Attach Gap
        space_attachment.setVisibility(attachmentview.getVisibility());
        if (attachmentview.getVisibility() == View.VISIBLE && TextUtils.isEmpty(message.getText()))
            space_attachment.setVisibility(View.GONE);

        // Reaction Gap
        space_reaction.setVisibility(rv_reaction.getVisibility());

        // ONLY_FOR_DEBUG
        if (false) {
            space_header.setBackgroundResource(R.color.stream_gap_header);
            space_same_user.setBackgroundResource(R.color.stream_gap_message);
            space_attachment.setBackgroundResource(R.color.stream_gap_attach);
            space_reaction.setBackgroundResource(R.color.stream_gap_reaction);
        }
    }

    protected void configUserAvatar() {
        avatar.setVisibility(isBottomPosition() ? View.VISIBLE : View.GONE);
        avatar.setUser(message.getUser(), style);
        avatar.setOnClickListener(view -> {
            userClickListener.onUserClick(message.getUser());
        });
    }

    protected void configUserNameAndMessageDateStyle() {
        if (!isBottomPosition()
                || (!style.isUserNameShow() && !style.isMessageDateShow())) {
            tv_username.setVisibility(View.GONE);
            tv_messagedate.setVisibility(View.GONE);
            return;
        }

        if (style.isUserNameShow() && messageListItem.isTheirs()) {
            tv_username.setVisibility(View.VISIBLE);
            tv_username.setText(message.getUser().getExtraValue("name", ""));
        } else {
            tv_username.setVisibility(View.GONE);
        }

        if (style.isMessageDateShow()) {
            tv_messagedate.setVisibility(View.VISIBLE);
            tv_messagedate.setText(TIME_DATEFORMAT.format(message.getCreatedAt()));
        } else {
            tv_messagedate.setVisibility(View.GONE);
        }
        style.messageUserNameText.apply(tv_username);

        if (messageListItem.isMine()) {
            style.messageDateTextMine.apply(tv_messagedate);
        } else {
            style.messageDateTextTheirs.apply(tv_messagedate);
        }
    }

    protected boolean isBottomPosition() {
        return positions.contains(MessageViewHolderFactory.Position.BOTTOM);
    }

    protected void configDeliveredIndicator() {
        iv_deliver.setVisibility(View.GONE);
        pb_deliver.setVisibility(View.GONE);

        Message lastMessage = LlcMigrationUtils.computeLastMessage(channel);

        if (isDeletedMessage()
                || isFailedMessage()
                || this.message == null
                || lastMessage == null
                || TextUtils.isEmpty(this.message.getId())
                || !messageListItem.getPositions().contains(MessageViewHolderFactory.Position.BOTTOM)
                || !messageListItem.getMessageReadBy().isEmpty()
                || !messageListItem.isMine()
                || this.message.getCreatedAt().getTime() < lastMessage.getCreatedAt().getTime()
                || this.message.getType().equals(ModelType.message_ephemeral)
                || isThread()
                || isEphemeral())
            return;


        //TODO: llc add sync
//        switch (this.message.getSyncStatus()) {
//            case Sync.LOCAL_ONLY:
//                pb_deliver.setVisibility(View.VISIBLE);
//                iv_deliver.setVisibility(View.GONE);
//                break;
//            case Sync.SYNCED:
//                pb_deliver.setVisibility(View.GONE);
//                iv_deliver.setVisibility(View.VISIBLE);
//                break;
//            case Sync.IN_MEMORY: // Same as LOCAL_FAILED
//            case Sync.LOCAL_FAILED:
//                pb_deliver.setVisibility(View.GONE);
//                iv_deliver.setVisibility(View.GONE);
//                break;
//        }
    }

    protected void configReadIndicator() {
        List<ChannelUserRead> readBy = messageListItem.getMessageReadBy();
        if (isDeletedMessage()
                || isFailedMessage()
                || readBy.isEmpty()
                || isThread()
                || isEphemeral()) {
            read_state.setVisibility(View.GONE);
            return;
        }
        read_state.setVisibility(View.VISIBLE);
        read_state.setReads(readBy, messageListItem.isTheirs(), style);
        read_state.setOnClickListener(view -> {
            readStateClickListener.onReadStateClick(readBy);
        });
    }

    protected void configMessageText() {
        if ((TextUtils.isEmpty(message.getText())
                && !isDeletedMessage())) {
            tv_text.setVisibility(View.GONE);
            return;
        }
        tv_text.setVisibility(View.VISIBLE);
        // Set Text
        configMessageTextViewText();
        // Set Style
        configMessageTextStyle();
        // Set Background
        configMessageTextBackground();
        // Set Click Listener
        configMessageTextClickListener();
    }

    protected void configMessageTextViewText() {
        if (isFailedMessage()) {
            // Set Failed Message Title Text
            SpannableStringBuilder builder = new SpannableStringBuilder();
            int failedDes = TextUtils.isEmpty(message.getCommand()) ? R.string.stream_message_failed_send : R.string.stream_message_invalid_command;
            SpannableString str1 = new SpannableString(context.getResources().getText(failedDes));
            str1.setSpan(new ForegroundColorSpan(Color.GRAY), 0, str1.length(), 0);
            str1.setSpan(new RelativeSizeSpan(0.7f), 0, str1.length(), 0);
            builder.append(str1);
            builder.append("\n");
            // Set Failed Message Description Text
            SpannableString str2 = new SpannableString(message.getText());
            builder.append(str2);
            tv_text.setText(builder, TextView.BufferType.SPANNABLE);
            return;
        }

        String text = StringUtility.getDeletedOrMentionedText(message);
        ChatMarkdown markdown = Chat.getInstance().getMarkdown();
        markdown.setText(tv_text, text);
    }

    protected void configMessageTextStyle() {
        if (isDeletedMessage()) {
            tv_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.stream_message_deleted_text_font_size));
            tv_text.setTextColor(context.getResources().getColor(R.color.stream_gray_dark));
            return;
        }

        if (messageListItem.isMine()) {
            style.messageTextMine.apply(tv_text);
        } else {
            style.messageTextTheirs.apply(tv_text);
        }

        if (style.getMessageLinkTextColor(messageListItem.isMine()) != 0)
            tv_text.setLinkTextColor(style.getMessageLinkTextColor(messageListItem.isMine()));
    }

    protected void configMessageTextBackground() {
        Drawable background;
        if (isFailedMessage()) {
            background = bubbleHelper.getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
        } else if (isDeletedMessage() || StringUtility.isEmoji(message.getText())) {
            background = null;
        } else {
            if (!message.getAttachments().isEmpty() && !message.getAttachments().isEmpty())
                background = bubbleHelper.getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), Arrays.asList(MessageViewHolderFactory.Position.MIDDLE));
            else
                background = bubbleHelper.getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
        }
        if (background != null)
            tv_text.setBackground(background);
        else
            tv_text.setBackgroundResource(0);
    }

    protected boolean isLongClick = false;

    protected void configMessageTextClickListener() {
        tv_text.setOnClickListener(view -> {

            if (isFailedMessage() && !ChatClient.instance().isSocketConnected())
                return;
            messageClickListener.onMessageClick(message, position);
        });

        tv_text.setOnLongClickListener(view -> {
            if (isDeletedMessage() || isFailedMessage()) return true;
            isLongClick = true;
            this.messageLongClickListener.onMessageLongClick(message);
            return true;
        });

        tv_text.setMovementMethod(new Utils.TextViewLinkHandler() {
            @Override
            public void onLinkClick(String url) {
                if (isDeletedMessage() || isFailedMessage()) return;
                if (isLongClick) {
                    isLongClick = false;
                    return;
                }
                Chat.getInstance().getNavigator().navigate(new WebLinkDestination(url, context));
            }
        });
    }

    protected void configAttachmentView() {
        boolean deletedMessage = isDeletedMessage();
        boolean failedMessage = isFailedMessage();
        boolean noAttachments = this.message.getAttachments().isEmpty();
        if (deletedMessage || failedMessage || noAttachments) {
            ChatLogger.Companion.getInstance().logE(getClass().getSimpleName(), "attachment hidden: deletedMessage:" + deletedMessage + ", failedMessage:" + failedMessage + " noAttachments:" + noAttachments);
            attachmentview.setVisibility(View.GONE);
            return;
        }

        attachmentview.setVisibility(View.VISIBLE);
        attachmentview.setViewHolderFactory(viewHolderFactory);
        attachmentview.setStyle(style);
        attachmentview.setGiphySendListener(giphySendListener);
        attachmentview.setEntity(this.messageListItem);
        attachmentview.setBubbleHelper(bubbleHelper);
        attachmentview.setAttachmentClickListener(attachmentClickListener);
        attachmentview.setLongClickListener(messageLongClickListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void configReactionView() {
        if (isDeletedMessage()
                || isFailedMessage()
                || !style.isReactionEnabled()
                || !channel.getConfig().isReactionsEnabled()
                || message.getReactionCounts() == null
                || message.getReactionCounts().size() == 0) {
            rv_reaction.setVisibility(View.GONE);
            iv_tail.setVisibility(View.GONE);
            space_reaction_tail.setVisibility(View.GONE);
            return;
        }
        configStyleReactionView();
        rv_reaction.setVisibility(View.VISIBLE);
        iv_tail.setVisibility(View.VISIBLE);
        space_reaction_tail.setVisibility(View.VISIBLE);
        rv_reaction.setAdapter(new ReactionListItemAdapter(context,
                message.getReactionCounts(),
                LlcMigrationUtils.getReactionTypes(),
                style));
        rv_reaction.setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP)
                reactionViewClickListener.onReactionViewClick(message);
            return false;
        });
    }

    protected void configReplyView() {
        int replyCount = message.getReplyCount();
        if (!style.isThreadEnabled()
                || !channel.getConfig().isRepliesEnabled()
                || (position == 0 && TextUtils.isEmpty(message.getId()))
                || isDeletedMessage()
                || isFailedMessage()
                || replyCount == 0
                || isThread()) {
            iv_reply.setVisibility(View.GONE);
            tv_reply.setVisibility(View.GONE);
            return;
        }
        iv_reply.setVisibility(View.VISIBLE);
        tv_reply.setVisibility(View.VISIBLE);
        tv_reply.setText(tv_reply.getContext().getResources().getQuantityString(R.plurals.stream_reply_count, replyCount, replyCount));

        iv_reply.setOnClickListener(view -> {
            messageClickListener.onMessageClick(message, position);
        });
        tv_reply.setOnClickListener(view -> {
            messageClickListener.onMessageClick(message, position);
        });
    }


    // endregion

    // region Layout Params
    protected void configMarginStartEnd() {
        configMarginStartEnd_(tv_text);
        configMarginStartEnd_(attachmentview);
        configMarginStartEnd_(iv_reply);
        configMarginStartEnd_(tv_username);
        configMarginStartEnd_(tv_messagedate);
    }

    protected void configMarginStartEnd_(View view) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        if (view.equals(tv_username)) {
            params.leftMargin = Utils.dpToPx(10 + 5) + avatarWidth;
            view.setLayoutParams(params);
            return;
        }
        if (view.equals(tv_messagedate)) {
            params.rightMargin = Utils.dpToPx(15 + 5) + avatarWidth;
            view.setLayoutParams(params);
            return;
        }
        params.leftMargin = Utils.dpToPx(10 + 5) + avatarWidth;
        params.rightMargin = Utils.dpToPx(15 + 5) + avatarWidth;
        view.setLayoutParams(params);
    }

    protected void configParamsMessageText() {
        if (tv_text.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_text.getLayoutParams();
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_text.setLayoutParams(params);
    }

    protected void configParamsMessageDate() {
        if (tv_messagedate.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_messagedate.getLayoutParams();
        if (!style.isUserNameShow() && style.isMessageDateShow()) {
            set.clone((ConstraintLayout) itemView);
            set.clear(R.id.tv_messagedate, ConstraintSet.START);
            set.applyTo((ConstraintLayout) itemView);
            params.startToStart = getActiveContentViewResId();
        }
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_messagedate.setLayoutParams(params);
    }

    protected void configParamsReactionSpace() {
        if (iv_tail.getVisibility() != View.VISIBLE) return;
        set.clone((ConstraintLayout) itemView);
        set.clear(R.id.space_reaction_tail, ConstraintSet.START);
        set.clear(R.id.space_reaction_tail, ConstraintSet.END);
        set.applyTo((ConstraintLayout) itemView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) space_reaction_tail.getLayoutParams();
        int activeContentViewResId = getActiveContentViewResId();

        if (messageListItem.isMine())
            params.endToStart = activeContentViewResId;
        else
            params.startToEnd = activeContentViewResId;

        space_reaction_tail.setLayoutParams(params);
        rv_reaction.post(() -> {
            params.width = rv_reaction.getHeight() / 3;
            space_reaction_tail.setLayoutParams(params);
        });
    }

    protected void configParamsReactionTail() {
        if (iv_tail.getVisibility() != View.VISIBLE) return;
        set.clone((ConstraintLayout) itemView);
        set.clear(R.id.iv_tail, ConstraintSet.START);
        set.clear(R.id.iv_tail, ConstraintSet.END);
        set.applyTo((ConstraintLayout) itemView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) iv_tail.getLayoutParams();
        if (messageListItem.isMine())
            params.startToStart = space_reaction_tail.getId();
        else
            params.endToEnd = space_reaction_tail.getId();
        rv_reaction.post(() -> {
            params.height = rv_reaction.getHeight();
            params.width = rv_reaction.getHeight();
            params.topMargin = rv_reaction.getHeight() / 3;
            iv_tail.setLayoutParams(params);
        });
    }

    protected void configParamsReactionRecycleView() {
        if (rv_reaction.getVisibility() != View.VISIBLE) return;
        rv_reaction.setVisibility(View.INVISIBLE);
        iv_tail.setVisibility(View.INVISIBLE);
        rv_reaction.post(() -> {
            if (rv_reaction.getVisibility() == View.GONE) return;
            set.clone((ConstraintLayout) itemView);
            set.clear(R.id.reactionsRecyclerView, ConstraintSet.START);
            set.clear(R.id.reactionsRecyclerView, ConstraintSet.END);
            set.applyTo((ConstraintLayout) itemView);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) rv_reaction.getLayoutParams();
            if (!message.getAttachments().isEmpty()) {
                if (messageListItem.isMine())
                    params.startToStart = R.id.space_reaction_tail;
                else
                    params.endToEnd = R.id.space_reaction_tail;
            } else {
                @DimenRes
                int reactionMargin = context.getResources().getDimensionPixelSize(R.dimen.stream_reaction_margin);
                if (tv_text.getWidth() + reactionMargin < rv_reaction.getWidth()) {
                    if (messageListItem.isMine())
                        params.endToEnd = R.id.tv_text;
                    else
                        params.startToStart = R.id.tv_text;
                } else {
                    if (messageListItem.isMine())
                        params.startToStart = R.id.space_reaction_tail;
                    else
                        params.endToEnd = R.id.space_reaction_tail;
                }
            }
            rv_reaction.setLayoutParams(params);
            rv_reaction.setVisibility(View.VISIBLE);
            iv_tail.setVisibility(View.VISIBLE);
            configParamsReadIndicator();
        });
    }

    protected void configParamsUserAvatar() {
        if (avatar.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) avatar.getLayoutParams();
        int marginStart = (int) context.getResources().getDimension(R.dimen.stream_message_avatar_margin);
        if (messageListItem.isTheirs()) {
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMarginStart(marginStart);
            params.setMarginEnd(0);
            params.horizontalBias = 0f;
        } else {
            marginStart = Utils.dpToPx(15);
            params.setMarginStart(0);
            params.setMarginEnd(marginStart);
            params.horizontalBias = 1f;
        }
        avatar.setLayoutParams(params);
    }

    protected void configParamsReply() {
        if (iv_reply.getVisibility() != View.VISIBLE) return;
        // Clear Constraint
        set.clone((ConstraintLayout) itemView);
        set.clear(R.id.tv_reply, ConstraintSet.START);
        set.clear(R.id.tv_reply, ConstraintSet.END);
        set.clear(R.id.iv_reply, ConstraintSet.START);
        set.clear(R.id.iv_reply, ConstraintSet.END);
        set.applyTo((ConstraintLayout) itemView);

        ConstraintLayout.LayoutParams paramsArrow = (ConstraintLayout.LayoutParams) iv_reply.getLayoutParams();
        ConstraintLayout.LayoutParams paramsText = (ConstraintLayout.LayoutParams) tv_reply.getLayoutParams();
        // Set Constraint
        if (messageListItem.isTheirs()) {
            iv_reply.setBackgroundResource(R.drawable.stream_ic_reply_incoming);
            paramsArrow.horizontalBias = 0f;
            paramsArrow.startToStart = getActiveContentViewResId();
            paramsText.startToEnd = iv_reply.getId();
        } else {
            iv_reply.setBackgroundResource(R.drawable.stream_ic_reply_outgoing);
            paramsArrow.horizontalBias = 1f;
            paramsArrow.endToEnd = getActiveContentViewResId();
            paramsText.endToStart = iv_reply.getId();
        }
        iv_reply.setLayoutParams(paramsArrow);
        tv_reply.setLayoutParams(paramsText);
    }

    public void configParamsReadIndicator() {
        if (read_state.getVisibility() != View.VISIBLE) return;

        set.clone((ConstraintLayout) itemView);
        set.clear(R.id.read_state, ConstraintSet.START);
        set.clear(R.id.read_state, ConstraintSet.END);
        set.clear(R.id.read_state, ConstraintSet.BOTTOM);
        set.applyTo((ConstraintLayout) itemView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) read_state.getLayoutParams();


        if (messageListItem.isMine())
            params.endToStart = getActiveContentViewResId();
        else
            params.startToEnd = getActiveContentViewResId();

        params.bottomToBottom = getActiveContentViewResId();
        params.leftMargin = Utils.dpToPx(8);
        params.rightMargin = Utils.dpToPx(8);
        read_state.setLayoutParams(params);
    }

    @IdRes
    protected int getActiveContentViewResId() {
        if (!message.getAttachments().isEmpty())
            return attachmentview.getId();
        else
            return tv_text.getId();
    }


    protected void configStyleReactionView() {
        if (style.getReactionViewBgDrawable() == -1) {
            rv_reaction.setBackground(new DrawableBuilder()
                    .rectangle()
                    .rounded()
                    .solidColor(style.getReactionViewBgColor())
                    .solidColorPressed(Color.LTGRAY)
                    .build());

            if (messageListItem.isMine())
                iv_tail.setImageDrawable(context.getResources().getDrawable(R.drawable.stream_tail_outgoing));
            else
                iv_tail.setImageDrawable(context.getResources().getDrawable(R.drawable.stream_tail_incoming));

            DrawableCompat.setTint(iv_tail.getDrawable(), style.getReactionViewBgColor());
        } else {
            int drawable = style.getReactionViewBgDrawable();
            rv_reaction.setBackground(context.getDrawable(drawable));
            iv_tail.setVisibility(View.GONE);
        }
    }


    protected boolean isDeletedMessage() {
        return message.getDeletedAt() != null;
    }

    protected boolean isFailedMessage() {

        return message.getSyncStatus() == SyncStatus.FAILED_PERMANENTLY || message.getType().equals(ModelType.message_error);
    }

    protected boolean isThread() {
        return !(message == null || TextUtils.isEmpty(message.getParentId()));
    }

    protected boolean isEphemeral() {
        return (message != null
                && message.getType().equals(ModelType.message_ephemeral));
    }
}
