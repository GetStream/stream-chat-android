package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.MessageTagModel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.Constant;
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

public class MessageListItemViewHolder extends BaseMessageListItemViewHolder {

    final String TAG = MessageListItemViewHolder.class.getSimpleName();

    private ConstraintLayout cl_message;
    private TextView tv_text, tv_deleted;
    private RecyclerView rv_reaction;

    private LinearLayout ll_send_failed;
    private TextView tv_failed_text, tv_failed_des;
    private AvatarGroupView<MessageListViewStyle> avatar;
    private ImageView iv_docket;
    private TextView tv_reactiontail_space, tv_reaction_space;

    private TextView tv_gap_header, tv_gap_sameUser, tv_gap_reaction, tv_gap_media_file, tv_gap_attach;
    private TextView tv_username, tv_messagedate;

    // Delivered Indicator
    private ReadStateView read_state;
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
    private MessageListView.UserClickListener userClickListener;

    private int position;
    private boolean isThread;
    private boolean isThreadHeader = false;
    private Context context;
    private Message message;
    private MessageListItem messageListItem;
    private MessageListViewStyle style;
    private List<MessageViewHolderFactory.Position> positions;

    @DimenRes int avatarWidth;

    public MessageListItemViewHolder(int resId, ViewGroup viewGroup, MessageListViewStyle s) {
        this(resId, viewGroup);
        style = s;
    }

    public void setStyle(MessageListViewStyle style) {
        this.style = style;
        avatarWidth = style.getAvatarWidth();
    }

    public MessageListItemViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);

        cl_message = itemView.findViewById(R.id.cl_message);
        rv_reaction = itemView.findViewById(R.id.rv_reaction);
        iv_docket = itemView.findViewById(R.id.iv_docket);
        tv_reactiontail_space = itemView.findViewById(R.id.tv_reactiontail_space);
        tv_reaction_space = itemView.findViewById(R.id.tv_reaction_space);

        tv_text = itemView.findViewById(R.id.tv_text);
        tv_deleted = itemView.findViewById(R.id.tv_deleted);

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
    }

    @Override
    public void bind(Context context,
                     ChannelState channelState,
                     MessageListItem messageListItem,
                     int position,
                     boolean isThread,
                     MessageListView.MessageClickListener messageClickListener,
                     MessageListView.MessageLongClickListener messageLongClickListener,
                     MessageListView.AttachmentClickListener attachmentClickListener,
                     MessageListView.UserClickListener userClickListener) {

        // set binding
        this.context = context;
        this.channelState = channelState;
        this.position = position;
        this.isThread = isThread;
        this.messageClickListener = messageClickListener;
        this.messageLongClickListener = messageLongClickListener;
        this.attachmentClickListener = attachmentClickListener;
        this.userClickListener = userClickListener;

        this.messageListItem = messageListItem;
        this.message = messageListItem.getMessage();
        this.positions = messageListItem.getPositions();

        init();
    }

    private void init() {
        // Configure UIs
        configSendFailed();
        configMessageText();
        configAttachmentView();
        configReactionView();
        configReplyView();
        configDelieveredIndicator();
        // apply position related style tweaks
        configPositionsStyle();
        // Configure Laytout Params
        configMarginStartEnd();
        configParamsMessageText();
        configParamsDeletedMessage();
        configParamsUserAvatar();
        configParamsDeliveredIndicator();
        configParamsReactionTailSpace();
        configParamsReactionSpace();
        configParamsReactionTail();
        configParamsReactionRecycleView();
        configParamsMessageDate();
        configParamsReply();
        configParamsReadState();
    }
    // endregion

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

            if (message.getDate() == null) message.setStartDay(Arrays.asList(message), null);
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

    private void configDelieveredIndicator() {
        iv_deliver.setVisibility(View.GONE);
        pb_deliver.setVisibility(View.GONE);

        if (isDeletedOrFailedMessage())  return;
        if (message == null || TextUtils.isEmpty(message.getId())) return;
        List<ChannelUserRead> readBy = messageListItem.getMessageReadBy();
        if (!readBy.isEmpty() || !messageListItem.isMine()) return;

        if (!messageListItem.getPositions().contains(MessageViewHolderFactory.Position.BOTTOM))
            return;
        if (message.isDelivered()) {
            iv_deliver.setVisibility(View.VISIBLE);
        } else {
            if (message.getCreatedAt().getTime() <= channelState.getChannel().getLastMessageDate().getTime()
                    && channelState.getLastMessage().getId().equals(message.getId())){
                message.setDelivered(true);
                iv_deliver.setVisibility(View.VISIBLE);
                return;
            }
            pb_deliver.setVisibility(View.VISIBLE);
        }
    }

    private void configParamsDeliveredIndicator() {
        List<ChannelUserRead> readBy = messageListItem.getMessageReadBy();

        if (readBy.size() == 0) {
            read_state.setVisibility(View.GONE);
        } else {
            read_state.setVisibility(View.VISIBLE);
            read_state.setReads(readBy, messageListItem.isTheirs(), style);
        }
    }

    private void configSendFailed() {
        if (message.getType().equals(ModelType.message_error)) {
            ll_send_failed.setVisibility(View.VISIBLE);
            tv_failed_text.setText(message.getText());
            int failedDes = TextUtils.isEmpty(message.getCommand()) ? R.string.stream_message_failed_send : R.string.stream_message_invalid_command;
            tv_failed_des.setText(context.getResources().getText(failedDes));
            //TODO what does this do?
            // int background = containerStyleOne(position) ? R.drawable.round_outgoing_failed1 : R.drawable.round_outgoing_failed2;
            //ll_send_failed.setBackgroundResource(background);

            ll_send_failed.setOnClickListener((View v) -> {
                if (messageClickListener != null) {
                    String tag = TextUtils.isEmpty(message.getCommand()) ? Constant.TAG_MESSAGE_RESEND : Constant.TAG_MESSAGE_INVALID_COMMAND;
                    v.setTag(new MessageTagModel(tag, position));
                    messageClickListener.onMessageClick(message, position);
                }
            });

        } else {
            ll_send_failed.setVisibility(View.GONE);
        }
    }

    private void configMessageText() {
        // Check Deleted Message
        if (message.getDeletedAt() != null) {
            tv_text.setVisibility(View.GONE);
            tv_deleted.setVisibility(View.VISIBLE);
            return;
        }
        if (message.getType().equals(ModelType.message_error)) {
            tv_text.setVisibility(View.GONE);
            tv_deleted.setVisibility(View.GONE);
            return;
        }
        if (TextUtils.isEmpty(message.getText())) {
            tv_text.setVisibility(View.GONE);
            tv_deleted.setVisibility(View.GONE);
        } else {
            if (message.getText().equals(Constant.MESSAGE_DELETED)) {
                tv_text.setVisibility(View.GONE);
                tv_deleted.setVisibility(View.VISIBLE);
            } else {
                tv_text.setVisibility(View.VISIBLE);
                tv_deleted.setVisibility(View.GONE);
            }
        }
        if (tv_text.getVisibility() != View.VISIBLE) return;

        // Set Text
        if (markwon == null)
            markwon = Markwon.builder(context)
                    .usePlugin(CorePlugin.create())
                    .usePlugin(LinkifyPlugin.create())
                    .build();
        markwon.setMarkdown(tv_text, Utils.getMentionedText(message));
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
            Log.i(TAG, "Long onUserClick: " + position);
            if (this.messageLongClickListener != null) {
                view.setTag(String.valueOf(position));
                this.messageLongClickListener.onMessageLongClick(message);
            }
            return true;
        });
    }

    private void configAttachmentView() {
        if (isDeletedOrFailedMessage()) {
            alv_attachments.setVisibility(View.GONE);
            return;
        }
        if (this.message.getAttachments() == null || this.message.getAttachments().isEmpty()) {
            alv_attachments.setVisibility(View.GONE);
        } else {
            alv_attachments.setVisibility(View.VISIBLE);
            alv_attachments.setViewHolderFactory(viewHolderFactory);
            alv_attachments.setStyle(style);
            alv_attachments.setEntity(this.messageListItem);
            alv_attachments.setBubbleHelper(this.getBubbleHelper());
            alv_attachments.setAttachmentClickListener(attachmentClickListener);
            alv_attachments.setLongClickListener(messageLongClickListener);
            boolean hasBackground = false;
            for (Attachment attachment : message.getAttachments()){
                if(!TextUtils.isEmpty(attachment.getText())){
                    hasBackground = true;
                    break;
                }
            }
            if (!hasBackground) {
                alv_attachments.setBackgroundResource(0);
                return;
            }
            Drawable background = getBubbleHelper().getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
            alv_attachments.setBackground(background);
        }
    }

    private void configReactionView() {
        if (!style.isEnableReaction()) {
            rv_reaction.setVisibility(View.GONE);
            iv_docket.setVisibility(View.GONE);
            return;
        }
        if (isDeletedOrFailedMessage()) {
            rv_reaction.setVisibility(View.GONE);
            iv_docket.setVisibility(View.GONE);
            return;
        }
        if (message.getReactionCounts() == null) {
            rv_reaction.setVisibility(View.GONE);
            iv_docket.setVisibility(View.GONE);
            return;
        }
        if (message.getReactionCounts().size() == 0) {
            rv_reaction.setVisibility(View.GONE);
            iv_docket.setVisibility(View.GONE);
            return;
        }

        rv_reaction.setVisibility(View.VISIBLE);
        iv_docket.setVisibility(View.VISIBLE);

        rv_reaction.setAdapter(new ReactionListItemAdapter(context, message.getReactionCounts(), channelState.getChannel().getReactionTypes()));
        if (messageListItem.isMine())
            iv_docket.setBackgroundResource(R.drawable.stream_ic_docket_incoming);
        else
            iv_docket.setBackgroundResource(R.drawable.stream_ic_docket_outgoing);
    }

    private void configReplyView() {
        if (this.isThread) {
            cl_reply.setVisibility(View.GONE);
            return;
        }
        if (isDeletedOrFailedMessage()) {
            cl_reply.setVisibility(View.GONE);
            return;
        }
        if (message.getReplyCount() > 0) cl_reply.setVisibility(View.VISIBLE);
        else {
            cl_reply.setVisibility(View.GONE);
            return;
        }
        if (message.getReplyCount() == 1) tv_reply.setText("1" + " reply");
        if (message.getReplyCount() > 1) tv_reply.setText(message.getReplyCount() + " replies");

        cl_reply.setOnClickListener((View v) -> {

        });
    }


    // endregion

    // region Layout Params
    private void configMarginStartEnd(){
        configMarginStartEnd_(tv_text);
        configMarginStartEnd_(alv_attachments);
        configMarginStartEnd_(tv_deleted);
        configMarginStartEnd_(ll_send_failed);
        configMarginStartEnd_(cl_reply);
        configMarginStartEnd_(tv_username);
        configMarginStartEnd_(tv_messagedate);
    }

    private void configMarginStartEnd_(View view){
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        if (view.equals(tv_username)){
            params.leftMargin = Utils.dpToPx(10 + 5) + avatarWidth;
            view.setLayoutParams(params);
            return;
        }
        if (view.equals(tv_messagedate)){
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

    private void configParamsDeletedMessage() {
        if (tv_deleted.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_deleted.getLayoutParams();
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_deleted.setLayoutParams(params);
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

    private void configParamsReactionTailSpace() {
        if (iv_docket.getVisibility() != View.VISIBLE) return;
        ConstraintSet set = new ConstraintSet();
        set.clone(cl_message);
        set.clear(R.id.tv_reactiontail_space, ConstraintSet.START);
        set.clear(R.id.tv_reactiontail_space, ConstraintSet.END);
        set.applyTo(cl_message);

        @IdRes int layoutId;
        if (this.message.getAttachments() == null || this.message.getAttachments().isEmpty()) {
            layoutId = tv_text.getId();
        } else {
            layoutId = alv_attachments.getId();
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_reactiontail_space.getLayoutParams();
        if (messageListItem.isMine())
            params.endToStart = layoutId;
        else
            params.startToEnd = layoutId;
        tv_reactiontail_space.setLayoutParams(params);
    }

    private void configParamsReactionSpace() {
        if (iv_docket.getVisibility() != View.VISIBLE) return;
        ConstraintSet set = new ConstraintSet();
        set.clone(cl_message);
        set.clear(R.id.tv_reaction_space, ConstraintSet.START);
        set.clear(R.id.tv_reaction_space, ConstraintSet.END);
        set.applyTo(cl_message);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_reaction_space.getLayoutParams();
        if (messageListItem.isMine())
            params.endToStart = R.id.tv_reactiontail_space;
        else
            params.startToEnd = R.id.tv_reactiontail_space;
        tv_reaction_space.setLayoutParams(params);
    }

    private void configParamsReactionTail() {
        if (iv_docket.getVisibility() != View.VISIBLE) return;
        ConstraintSet set = new ConstraintSet();
        set.clone(cl_message);
        set.clear(R.id.iv_docket, ConstraintSet.START);
        set.clear(R.id.iv_docket, ConstraintSet.END);
        set.applyTo(cl_message);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) iv_docket.getLayoutParams();
        if (messageListItem.isMine())
            params.startToStart = tv_reactiontail_space.getId();
        else
            params.endToEnd = tv_reactiontail_space.getId();
        iv_docket.setLayoutParams(params);
    }

    private void configParamsReactionRecycleView() {
        if (rv_reaction.getVisibility() != View.VISIBLE) return;
        rv_reaction.setVisibility(View.INVISIBLE);
        iv_docket.setVisibility(View.INVISIBLE);
        rv_reaction.post(() -> {
            ConstraintSet set = new ConstraintSet();
            set.clone(cl_message);
            set.clear(R.id.rv_reaction, ConstraintSet.START);
            set.clear(R.id.rv_reaction, ConstraintSet.END);
            set.applyTo(cl_message);

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
            iv_docket.setVisibility(View.VISIBLE);
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
        ConstraintSet set = new ConstraintSet();
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

    public void configParamsReadState() {
        if (read_state.getVisibility() != View.VISIBLE) return;

        ConstraintSet set = new ConstraintSet();
        set.clone(cl_message);
        set.clear(R.id.read_state, ConstraintSet.START);
        set.clear(R.id.read_state, ConstraintSet.END);
        set.applyTo(cl_message);

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

        params.leftMargin = Utils.dpToPx(3);
        params.rightMargin = Utils.dpToPx(3);
        read_state.setLayoutParams(params);
    }


    public void setViewHolderFactory(MessageViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }

    private boolean isDeletedOrFailedMessage(){
        return tv_deleted.getVisibility() == View.VISIBLE ||
                ll_send_failed.getVisibility() == View.VISIBLE;
    }
}
