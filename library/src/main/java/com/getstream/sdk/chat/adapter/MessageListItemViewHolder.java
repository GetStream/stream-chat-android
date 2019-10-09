package com.getstream.sdk.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.IdRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.MessageStatus;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.AvatarGroupView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;
import com.getstream.sdk.chat.view.ReadStateView;

import java.util.Arrays;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import top.defaults.drawabletoolbox.DrawableBuilder;

public class MessageListItemViewHolder extends BaseMessageListItemViewHolder {

    final String TAG = MessageListItemViewHolder.class.getSimpleName();
    @DimenRes
    int avatarWidth;
    private TextView tv_text;
    private RecyclerView rv_reaction;
    private LinearLayout ll_send_failed;
    private TextView tv_failed_text, tv_failed_des;
    private AvatarGroupView<MessageListViewStyle> avatar;
    private ImageView iv_tail;
    private TextView tv_reaction_space;
    private TextView tv_gap_header, tv_gap_sameUser, tv_gap_reaction, tv_gap_media_file, tv_gap_attach;
    private TextView tv_username, tv_messagedate;
    // Delivered Indicator
    private ReadStateView<MessageListViewStyle> read_state;
    private ProgressBar pb_deliver;
    private ImageView iv_deliver;
    private AttachmentListView alv_attachments;
    // Replay
    private ConstraintLayout cl_reply;
    private ImageView iv_reply;
    private TextView tv_reply;
    private Markwon markwon;
    private RecyclerView.LayoutManager mLayoutManager;
    private MessageViewHolderFactory viewHolderFactory;
    private ChannelState channelState;
    private MessageListView.MessageClickListener messageClickListener;
    private MessageListView.MessageLongClickListener messageLongClickListener;
    private MessageListView.AttachmentClickListener attachmentClickListener;
    private MessageListView.ReactionViewClickListener reactionViewClickListener;
    private MessageListView.UserClickListener userClickListener;
    private MessageListView.ReadStateClickListener readStateClickListener;

    private int position;
    private boolean isThread;
    private Context context;
    private Message message;
    private MessageListItem messageListItem;
    private MessageListViewStyle style;
    private MessageListView.GiphySendListener giphySendListener;
    private List<MessageViewHolderFactory.Position> positions;
    private ConstraintSet set;

    public MessageListItemViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);

        rv_reaction = itemView.findViewById(R.id.rv_reaction);
        iv_tail = itemView.findViewById(R.id.iv_tail);
        tv_reaction_space = itemView.findViewById(R.id.tv_reaction_space);

        tv_text = itemView.findViewById(R.id.tv_text);

        ll_send_failed = itemView.findViewById(R.id.ll_send_failed);
        tv_failed_des = itemView.findViewById(R.id.tv_failed_des);
        tv_failed_text = itemView.findViewById(R.id.tv_failed_text);
        avatar = itemView.findViewById(R.id.avatar);

        cl_reply = itemView.findViewById(R.id.cl_reply);
        iv_reply = itemView.findViewById(R.id.iv_reply);
        tv_reply = itemView.findViewById(R.id.tv_reply);

        tv_username = itemView.findViewById(R.id.tv_username);
        tv_messagedate = itemView.findViewById(R.id.tv_messagedate);

        tv_gap_header = itemView.findViewById(R.id.tv_gap_header);
        tv_gap_sameUser = itemView.findViewById(R.id.tv_gap_sameUser);
        tv_gap_reaction = itemView.findViewById(R.id.tv_gap_reaction);
        tv_gap_media_file = itemView.findViewById(R.id.tv_gap_media_file);
        tv_gap_attach = itemView.findViewById(R.id.tv_gap_attach);

        alv_attachments = itemView.findViewById(R.id.attachmentview);

        read_state = itemView.findViewById(R.id.read_state);
        pb_deliver = itemView.findViewById(R.id.pb_deliver);
        iv_deliver = itemView.findViewById(R.id.iv_deliver);

        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        rv_reaction.setHasFixedSize(true);
    }

    @Override
    public void bind(Context context,
                     ChannelState channelState,
                     MessageListItem messageListItem,
                     int position) {

        // set binding
        this.context = context;
        this.channelState = channelState;
        this.position = position;

        this.messageListItem = messageListItem;
        this.message = messageListItem.getMessage();
        this.positions = messageListItem.getPositions();
        this.set = new ConstraintSet();
        init();
    }

    // region Init
    private void init() {
        // Configure UIs
        configSendFailed();
        configMessageText();
        configAttachmentView();
        configReactionView();
        configReplyView();
        configDeliveredIndicator();
        configReadIndicator();
        // apply position related style tweaks
        configPositionsStyle();
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


    public void setStyle(MessageListViewStyle style) {
        this.style = style;
        avatarWidth = style.getAvatarWidth();
    }

    public void setThread(boolean thread) {
        isThread = thread;
    }

    public void setMessageClickListener(MessageListView.MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    public void setMessageLongClickListener(MessageListView.MessageLongClickListener messageLongClickListener) {
        this.messageLongClickListener = messageLongClickListener;
    }

    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }

    public void setReactionViewClickListener(MessageListView.ReactionViewClickListener reactionViewClickListener) {
        this.reactionViewClickListener = reactionViewClickListener;
    }

    public void setUserClickListener(MessageListView.UserClickListener userClickListener) {
        this.userClickListener = userClickListener;
    }

    public void setReadStateClickListener(MessageListView.ReadStateClickListener readStateClickListener) {
        this.readStateClickListener = readStateClickListener;
    }

    public void setGiphySendListener(MessageListView.GiphySendListener giphySendListener) {
        this.giphySendListener = giphySendListener;
    }

    // endregion

    // region Config
    private void configPositionsStyle() {
        // TOP position has a rounded top left corner and extra spacing
        // BOTTOM position shows the user avatar & message time
        tv_gap_header.setVisibility(View.GONE);
        tv_gap_sameUser.setVisibility(View.VISIBLE);
        // TOP
        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
            // extra spacing
            tv_gap_header.setVisibility(View.VISIBLE);
            tv_gap_sameUser.setVisibility(View.GONE);
        }
        // BOTTOM
        if (positions.contains(MessageViewHolderFactory.Position.BOTTOM)) {
            // show the date and user initials for the bottom message
            tv_username.setVisibility(View.VISIBLE);
            tv_messagedate.setVisibility(View.VISIBLE);
            avatar.setVisibility(View.VISIBLE);

            if (messageListItem.isTheirs()) {
                tv_username.setVisibility(View.VISIBLE);
                tv_username.setText(message.getUser().getName());
            } else {
                tv_username.setVisibility(View.GONE);
            }
            avatar.setUser(message.getUser(), style);
            avatar.setOnClickListener(view -> {
                if (userClickListener != null)
                    userClickListener.onUserClick(message.getUser());
            });

            if (message.getDate() == null) Message.setStartDay(Arrays.asList(message), null);
            if (message.getDate().equals("Today") || message.getDate().equals("Yesterday"))
                tv_messagedate.setText(message.getTime());
            else
                tv_messagedate.setText(message.getDate() + ", " + message.getTime());
        } else {
            tv_username.setVisibility(View.GONE);
            tv_messagedate.setVisibility(View.GONE);
            avatar.setVisibility(View.GONE);
        }

        // Attach Gap
        tv_gap_attach.setVisibility(alv_attachments.getVisibility());
        if (alv_attachments.getVisibility() == View.VISIBLE && TextUtils.isEmpty(message.getText()))
            tv_gap_attach.setVisibility(View.GONE);

        // Reaction Gap
        tv_gap_reaction.setVisibility(rv_reaction.getVisibility());

        // ONLY_FOR_DEBUG
        if (false) {
            tv_gap_header.setBackgroundResource(R.color.stream_gap_header);
            tv_gap_sameUser.setBackgroundResource(R.color.stream_gap_message);
            try {
                tv_gap_media_file.setBackgroundResource(R.color.stream_gap_media_file);
            } catch (Exception e) {
            }
            tv_gap_attach.setBackgroundResource(R.color.stream_gap_attach);
            tv_gap_reaction.setBackgroundResource(R.color.stream_gap_reaction);
        }
    }

    private void configDeliveredIndicator() {
        iv_deliver.setVisibility(View.GONE);
        pb_deliver.setVisibility(View.GONE);

        if (isDeletedOrFailedMessage()
                || message == null
                || TextUtils.isEmpty(message.getId())
                || !messageListItem.getPositions().contains(MessageViewHolderFactory.Position.BOTTOM)
                || !messageListItem.getMessageReadBy().isEmpty()
                || !messageListItem.isMine()
                || message.getCreatedAt().getTime() < channelState.getLastMessage().getCreatedAt().getTime()
                || message.getType().equals(ModelType.message_ephemeral)
                || message.getStatus() == null)
            return;

        switch (message.getStatus()) {
            case SENDING:
                pb_deliver.setVisibility(View.VISIBLE);
                iv_deliver.setVisibility(View.GONE);
                break;
            case RECEIVED:
                pb_deliver.setVisibility(View.GONE);
                iv_deliver.setVisibility(View.VISIBLE);
                break;
            case FAILED:
                pb_deliver.setVisibility(View.GONE);
                iv_deliver.setVisibility(View.GONE);
                break;
            default:
                if (message.getCreatedAt().getTime() <= channelState.getChannel().getLastMessageDate().getTime()
                        && channelState.getLastMessage().getId().equals(message.getId())) {
                    message.setStatus(MessageStatus.RECEIVED);
                    iv_deliver.setVisibility(View.VISIBLE);
                    return;
                }
                pb_deliver.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void configReadIndicator() {
        List<ChannelUserRead> readBy = messageListItem.getMessageReadBy();
        if (isDeletedOrFailedMessage()
                || readBy.isEmpty()
                || isThread) {
            read_state.setVisibility(View.GONE);
            return;
        }
        read_state.setVisibility(View.VISIBLE);
        read_state.setReads(readBy, messageListItem.isTheirs(), style);
        read_state.setOnClickListener(view -> {
            if (readStateClickListener != null)
                readStateClickListener.onReadStateClick(readBy);
        });
    }

    private void configSendFailed() {
        if (message.getStatus() == MessageStatus.FAILED
                || message.getType().equals(ModelType.message_error)) {
            ll_send_failed.setVisibility(View.VISIBLE);
            tv_failed_text.setText(message.getText());
            // Set Style
            tv_failed_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getMessageTextSizeMine());
            tv_failed_text.setTextColor(style.getMessageTextColorMine());
            tv_failed_text.setTypeface(Typeface.DEFAULT, style.getMessageTextStyleMine());

            int failedDes = TextUtils.isEmpty(message.getCommand()) ? R.string.stream_message_failed_send : R.string.stream_message_invalid_command;
            tv_failed_des.setText(context.getResources().getText(failedDes));
            Drawable background = getBubbleHelper().getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
            ll_send_failed.setBackground(background);

            ll_send_failed.setOnClickListener(view -> {
                if (!StreamChat.getInstance(context).isConnected()) return;
                if (messageClickListener != null)
                    messageClickListener.onMessageClick(message, position);
            });
        } else {
            ll_send_failed.setVisibility(View.GONE);
        }
    }

    boolean isLongClick = false;
    @SuppressLint("ClickableViewAccessibility")
    private void configMessageText() {
        if (message.getStatus() == MessageStatus.FAILED
                || message.getType().equals(ModelType.message_error)
                || (TextUtils.isEmpty(message.getText()) && message.getDeletedAt() == null)) {
            tv_text.setVisibility(View.GONE);
            return;
        }

        tv_text.setVisibility(View.VISIBLE);
        // Set Text
        if (markwon == null)
            markwon = Markwon.builder(context)
                    .usePlugin(CorePlugin.create())
                    .usePlugin(LinkifyPlugin.create())
                    .build();
        markwon.setMarkdown(tv_text, StringUtility.getDeletedOrMentionedText(message));
        // Deleted Message
        if (message.getDeletedAt() != null) {
            // background
            tv_text.setBackgroundResource(0);
            // style
            tv_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.stream_message_deleted_text_font_size));
            tv_text.setTextColor(context.getResources().getColor(R.color.stream_gray_dark));
            return;
        }
        // background
        if (StringUtility.isEmoji(message.getText())) {
            tv_text.setBackgroundResource(0);
        } else {
            Drawable background;
            if (message.getAttachments() != null && !message.getAttachments().isEmpty())
                background = getBubbleHelper().getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), Arrays.asList(MessageViewHolderFactory.Position.MIDDLE));
            else
                background = getBubbleHelper().getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());

            tv_text.setBackground(background);
        }
        // set style
        if (messageListItem.isMine()) {
            tv_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getMessageTextSizeMine());
            tv_text.setTextColor(style.getMessageTextColorMine());
            tv_text.setTypeface(Typeface.DEFAULT, style.getMessageTextStyleMine());
        } else {
            tv_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getMessageTextSizeTheirs());
            tv_text.setTextColor(style.getMessageTextColorTheirs());
            tv_text.setTypeface(Typeface.DEFAULT, style.getMessageTextStyleTheirs());
        }
        // Set Click Listener
        tv_text.setOnClickListener(view -> {
            Log.i(TAG, "onMessageClick: " + position);
            if (messageClickListener != null) {
                messageClickListener.onMessageClick(message, position);
            }
        });

        tv_text.setOnLongClickListener(view -> {
            isLongClick = true;
            if (this.messageLongClickListener != null)
                this.messageLongClickListener.onMessageLongClick(message);
            return true;
        });

        tv_text.setMovementMethod(new Utils.TextViewLinkHandler() {
            @Override
            public void onLinkClick(String url) {
                if (isLongClick){
                    isLongClick = false;
                    return;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            }
        });
    }


    private void configAttachmentView() {
        if (isDeletedOrFailedMessage()
                || this.message.getAttachments() == null
                || this.message.getAttachments().isEmpty()) {
            alv_attachments.setVisibility(View.GONE);
            return;
        }

        alv_attachments.setVisibility(View.VISIBLE);
        alv_attachments.setViewHolderFactory(viewHolderFactory);
        alv_attachments.setStyle(style);
        alv_attachments.setGiphySendListener(giphySendListener);
        alv_attachments.setEntity(this.messageListItem);
        alv_attachments.setBubbleHelper(this.getBubbleHelper());
        alv_attachments.setAttachmentClickListener(attachmentClickListener);
        alv_attachments.setLongClickListener(messageLongClickListener);

        for (Attachment attachment : message.getAttachments()) {
            if (!TextUtils.isEmpty(attachment.getText())
                    || !TextUtils.isEmpty(attachment.getTitle())) {
                Drawable background = getBubbleHelper().getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
                alv_attachments.setBackground(background);
                return;
            }
        }
        alv_attachments.setBackgroundResource(0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void configReactionView() {
        if (isDeletedOrFailedMessage()
                || !style.isReactionEnabled()
                || !channelState.getChannel().getConfig().isReactionsEnabled()
                || message.getReactionCounts() == null
                || message.getReactionCounts().size() == 0) {
            rv_reaction.setVisibility(View.GONE);
            iv_tail.setVisibility(View.GONE);
            tv_reaction_space.setVisibility(View.GONE);
            return;
        }
        configStyleReactionView();
        rv_reaction.setVisibility(View.VISIBLE);
        iv_tail.setVisibility(View.VISIBLE);
        tv_reaction_space.setVisibility(View.VISIBLE);
        rv_reaction.setAdapter(new ReactionListItemAdapter(context,
                message.getReactionCounts(),
                channelState.getChannel().getReactionTypes(),
                style));
        rv_reaction.setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP)
                reactionViewClickListener.onReactionViewClick(message);
            return false;
        });
    }

    private void configReplyView() {
        if (!style.isThreadEnabled()
                || !channelState.getChannel().getConfig().isRepliesEnabled()
                || isDeletedOrFailedMessage()
                || isThread
                || message.getReplyCount() == 0) {
            cl_reply.setVisibility(View.GONE);
            return;
        }

        cl_reply.setVisibility(View.VISIBLE);
        tv_reply.setText(message.getReplyCount() + (message.getReplyCount() == 1 ? " reply" : " replies"));

        cl_reply.setOnClickListener(view -> {
            if (messageClickListener != null)
                messageClickListener.onMessageClick(message, position);
        });
    }


    // endregion

    // region Layout Params
    private void configMarginStartEnd() {
        configMarginStartEnd_(tv_text);
        configMarginStartEnd_(alv_attachments);
        configMarginStartEnd_(ll_send_failed);
        configMarginStartEnd_(cl_reply);
        configMarginStartEnd_(tv_username);
        configMarginStartEnd_(tv_messagedate);
    }

    private void configMarginStartEnd_(View view) {
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

    private void configParamsMessageText() {
        if (tv_text.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_text.getLayoutParams();
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_text.setLayoutParams(params);
    }

    private void configParamsMessageDate() {
        if (tv_messagedate.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_messagedate.getLayoutParams();
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_messagedate.setLayoutParams(params);
    }

    private void configParamsReactionSpace() {
        if (iv_tail.getVisibility() != View.VISIBLE) return;
        set.clone((ConstraintLayout) itemView);
        set.clear(R.id.tv_reaction_space, ConstraintSet.START);
        set.clear(R.id.tv_reaction_space, ConstraintSet.END);
        set.applyTo((ConstraintLayout) itemView);

        @IdRes int layoutId;
        if (this.message.getAttachments() == null || this.message.getAttachments().isEmpty()) {
            layoutId = tv_text.getId();
        } else {
            layoutId = alv_attachments.getId();
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_reaction_space.getLayoutParams();
        if (messageListItem.isMine())
            params.endToStart = layoutId;
        else
            params.startToEnd = layoutId;
        tv_reaction_space.setLayoutParams(params);
        rv_reaction.post(() -> {
            params.width = rv_reaction.getHeight() / 3;
            tv_reaction_space.setLayoutParams(params);
        });
    }

    private void configParamsReactionTail() {
        if (iv_tail.getVisibility() != View.VISIBLE) return;
        set.clone((ConstraintLayout) itemView);
        set.clear(R.id.iv_tail, ConstraintSet.START);
        set.clear(R.id.iv_tail, ConstraintSet.END);
        set.applyTo((ConstraintLayout) itemView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) iv_tail.getLayoutParams();
        if (messageListItem.isMine())
            params.startToStart = tv_reaction_space.getId();
        else
            params.endToEnd = tv_reaction_space.getId();
        rv_reaction.post(() -> {
            params.height = rv_reaction.getHeight();
            params.width = rv_reaction.getHeight();
            params.topMargin = rv_reaction.getHeight() / 3;
            iv_tail.setLayoutParams(params);
        });
    }

    private void configParamsReactionRecycleView() {
        if (rv_reaction.getVisibility() != View.VISIBLE) return;
        rv_reaction.setVisibility(View.INVISIBLE);
        iv_tail.setVisibility(View.INVISIBLE);
        rv_reaction.post(() -> {
            if (rv_reaction.getVisibility() == View.GONE) return;
            set.clone((ConstraintLayout) itemView);
            set.clear(R.id.rv_reaction, ConstraintSet.START);
            set.clear(R.id.rv_reaction, ConstraintSet.END);
            set.applyTo((ConstraintLayout) itemView);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) rv_reaction.getLayoutParams();
            if (this.message.getAttachments() == null || this.message.getAttachments().isEmpty()) {
                @DimenRes
                int reactionMargin = context.getResources().getDimensionPixelSize(R.dimen.stream_reaction_margin);
                if (tv_text.getWidth() + reactionMargin < rv_reaction.getWidth()) {
                    if (messageListItem.isMine())
                        params.endToEnd = R.id.tv_text;
                    else
                        params.startToStart = R.id.tv_text;
                } else {
                    if (messageListItem.isMine())
                        params.startToStart = R.id.tv_reaction_space;
                    else
                        params.endToEnd = R.id.tv_reaction_space;
                }
            } else {
                if (messageListItem.isMine())
                    params.startToStart = R.id.tv_reaction_space;
                else
                    params.endToEnd = R.id.tv_reaction_space;
            }
            rv_reaction.setLayoutParams(params);
            rv_reaction.setVisibility(View.VISIBLE);
            iv_tail.setVisibility(View.VISIBLE);
            configParamsReadIndicator();
        });
    }

    private void configParamsUserAvatar() {
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

    private void configParamsReply() {
        if (cl_reply.getVisibility() != View.VISIBLE) return;
        // Clear Constraint
        set.clone(cl_reply);
        set.clear(R.id.tv_reply, ConstraintSet.START);
        set.clear(R.id.tv_reply, ConstraintSet.END);
        set.clear(R.id.iv_reply, ConstraintSet.START);
        set.clear(R.id.iv_reply, ConstraintSet.END);
        set.applyTo(cl_reply);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cl_reply.getLayoutParams();
        ConstraintLayout.LayoutParams paramsArrow = (ConstraintLayout.LayoutParams) iv_reply.getLayoutParams();
        ConstraintLayout.LayoutParams paramsText = (ConstraintLayout.LayoutParams) tv_reply.getLayoutParams();

        // Set Constraint
        if (messageListItem.isTheirs()) {
            iv_reply.setBackgroundResource(R.drawable.stream_ic_reply_incoming);
            params.horizontalBias = 0f;
            paramsText.endToEnd = cl_reply.getId();
            paramsArrow.startToStart = cl_reply.getId();
            paramsText.startToEnd = iv_reply.getId();
        } else {
            iv_reply.setBackgroundResource(R.drawable.stream_ic_reply_outgoing);
            params.horizontalBias = 1f;
            paramsArrow.endToEnd = cl_reply.getId();
            paramsText.startToStart = cl_reply.getId();
            paramsArrow.startToEnd = tv_reply.getId();
        }
        cl_reply.setLayoutParams(params);
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

        @IdRes int layoutId;
        if (this.message.getAttachments() == null || this.message.getAttachments().isEmpty()) {
            layoutId = tv_text.getId();
        } else {
            layoutId = alv_attachments.getId();
        }

        if (messageListItem.isMine())
            params.endToStart = layoutId;
        else
            params.startToEnd = layoutId;

        params.bottomToBottom = layoutId;
        params.leftMargin = Utils.dpToPx(8);
        params.rightMargin = Utils.dpToPx(8);
        read_state.setLayoutParams(params);
    }

    private void configStyleReactionView() {
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

    public void setViewHolderFactory(MessageViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }

    private boolean isDeletedOrFailedMessage() {
        return message.getDeletedAt() != null
                || message.getStatus() == MessageStatus.FAILED
                || message.getType().equals(ModelType.message_error);
    }
}
