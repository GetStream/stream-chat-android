package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.MessageTagModel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
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

    private ConstraintLayout cl_message, headerView;
    private TextView tv_text, tv_deleted;
    private RecyclerView rv_reaction;

    private LinearLayout ll_send_failed;
    private TextView tv_failed_text, tv_failed_des;
    private AvatarGroupView<MessageListViewStyle> avatar;
    private ImageView iv_docket;

    private TextView tv_gap_header, tv_gap_sameUser, tv_gap_reaction, tv_gap_media_file, tv_gap_attach;
    private TextView tv_username, tv_messagedate;

    // Delivered Indicator
    private ReadStateView read_state;
    private TextView tv_indicator_initials, tv_read_count;
    private ImageView cv_indicator_avatar;
    private ProgressBar pb_indicator;

    private AttachmentListView alv_attachments;
    // Replay
    private ConstraintLayout cl_reply;
    private ImageView iv_reply;
    private TextView tv_reply;

    private Markwon markwon;
    private RecyclerView.LayoutManager mLayoutManager;
    private MessageViewHolderFactory viewHolderFactory;

    private ChannelState channelState;
    private List<Message> messageList;
    private MessageListView.MessageClickListener messageClickListener;
    private MessageListView.AttachmentClickListener attachmentClickListener;
    private int position;
    private boolean isThread;
    private boolean isThreadHeader = false;
    private Context context;
    private Message message;
    private MessageListItem messageListItem;
    private MessageListViewStyle style;
    private List<MessageViewHolderFactory.Position> positions;

    public MessageListItemViewHolder(int resId, ViewGroup viewGroup, MessageListViewStyle s) {
        this(resId, viewGroup);
        style = s;
    }

    public void setStyle(MessageListViewStyle style) {
        this.style = style;

    }

    public MessageListItemViewHolder(int resId, ViewGroup viewGroup) {
        super(resId, viewGroup);

        cl_message = itemView.findViewById(R.id.cl_message);
        rv_reaction = itemView.findViewById(R.id.rv_reaction);
        iv_docket = itemView.findViewById(R.id.iv_docket);


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

        alv_attachments = itemView.findViewById(R.id.cl_attachment);

        read_state = itemView.findViewById(R.id.read_state);

        tv_indicator_initials = itemView.findViewById(R.id.tv_indicator_initials);
        tv_read_count = itemView.findViewById(R.id.tv_read_count);
        cv_indicator_avatar = itemView.findViewById(R.id.cv_indicator_avatar);
        pb_indicator = itemView.findViewById(R.id.pb_indicator);

        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
    }

    @Override
    public void bind(Context context, ChannelState channelState, MessageListItem messageListItem, int position, boolean isThread, MessageListView.MessageClickListener messageClickListener, MessageListView.AttachmentClickListener attachmentClickListener) {
        // set binding
        this.context = context;
        this.channelState = channelState;
        this.position = position;
        this.isThread = isThread;
        this.messageClickListener = messageClickListener;
        this.attachmentClickListener = attachmentClickListener;

        this.messageListItem = messageListItem;
        this.message = messageListItem.getMessage();
        this.positions = messageListItem.getPositions();


        if (this.message.getAttachments() == null || this.message.getAttachments().size() == 0) {
            alv_attachments.setVisibility(View.GONE);
        } else {
            alv_attachments.setVisibility(View.VISIBLE);
            alv_attachments.setViewHolderFactory(viewHolderFactory);
            alv_attachments.setStyle(style);
            alv_attachments.setEntity(this.messageListItem);
            alv_attachments.setBubbleHelper(this.getBubbleHelper());
            alv_attachments.setAttachmentClickListener(attachmentClickListener);
        }

        // apply the style based on mine or theirs
        if (messageListItem.isMine()) {
            this.applyStyleMine();
        } else {
            this.applyStyleTheirs();
        }

        // apply position related style tweaks
        this.applyPositionsStyle();


        // TODO: hook up click and longclick

        // Configure UIs
        configSendFailed();
        configMessageText();
        configReactionView();
        configReplyView();

        // Configure Laytout Params

        configParamsMessageText();
        configParamsDeletedMessage();
        configParamsUserAvatar();
        configParamsReactionRecycleView();
        configParamsDeliveredIndicator();
        configParamsReactionTail();
        configParamsMessageDate();
        configParamsReply();
        configParamsReadState();
//        configParamsAttachment();
    }
    // endregion

    private void applyStyleMine() {
        Drawable background = getBubbleHelper().getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
        tv_text.setBackground(background);
        tv_text.setTextColor(style.getMessageTextColorMine());
    }

    private void applyStyleTheirs() {
        Drawable background = getBubbleHelper().getDrawableForMessage(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
        tv_text.setBackground(background);
        tv_text.setTextColor(style.getMessageTextColorTheirs());
    }

    private void applyPositionsStyle() {
        // TOP position has a rounded top left corner and extra spacing
        // BOTTOM position shows the user avatar & message time

        // RESET the defaults
        tv_username.setVisibility(View.GONE);
        tv_messagedate.setVisibility(View.GONE);
        avatar.setVisibility(View.GONE);
        tv_gap_header.setVisibility(View.GONE);
        tv_gap_sameUser.setVisibility(View.VISIBLE);

        // TOP
        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
            // extra spacing
            tv_gap_header.setVisibility(View.VISIBLE);
            tv_gap_sameUser.setVisibility(View.GONE);
            // rounded corner


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
            } else
                tv_username.setVisibility(View.GONE);
            avatar.setUser(message.getUser(), style);
            if (message.getDate() == null) message.setStartDay(Arrays.asList(message), null);
            if (message.getDate().equals("Today") || message.getDate().equals("Yesterday"))
                tv_messagedate.setText(message.getTime());
            else
                tv_messagedate.setText(message.getDate() + ", " + message.getTime());
        }
    }


    private void configSendFailed() {
        if (message.getType().equals(ModelType.message_error)) {
            ll_send_failed.setVisibility(View.VISIBLE);
            tv_failed_text.setText(message.getText());
            int failedDes = TextUtils.isEmpty(message.getCommand()) ? R.string.message_failed_send : R.string.message_invalid_command;
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
        markwon.setMarkdown(tv_text, Global.getMentionedText(message));


        // Set Click Listener
        tv_text.setOnClickListener((View v) -> {
            Log.d(TAG, "onMessageClick: " + position);
            if (messageClickListener != null) {
                messageClickListener.onMessageClick(message, position);
            }
        });


    }

    private void configReactionView() {
        if (tv_deleted.getVisibility() == View.VISIBLE || ll_send_failed.getVisibility() == View.VISIBLE) {
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

        rv_reaction.setAdapter(new ReactionListItemAdapter(context, message.getReactionCounts()));
        if (messageListItem.isMine())
            iv_docket.setBackgroundResource(R.drawable.docket_incoming);
        else
            iv_docket.setBackgroundResource(R.drawable.docket_outgoing);
    }

    private void configReplyView() {
        if (this.isThread) {
            cl_reply.setVisibility(View.GONE);
            return;
        }
        if (tv_deleted.getVisibility() == View.VISIBLE || ll_send_failed.getVisibility() == View.VISIBLE) {
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

    private void configParamsReactionRecycleView() {
        if (rv_reaction.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) rv_reaction.getLayoutParams();
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        rv_reaction.setLayoutParams(params);
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

    private void configParamsReactionTail() {
        if (iv_docket.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) iv_docket.getLayoutParams();
        if (messageListItem.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        iv_docket.setLayoutParams(params);
    }

    private void configParamsUserAvatar() {
        if (avatar.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) avatar.getLayoutParams();
        int marginStart = (int) context.getResources().getDimension(R.dimen.message_avatar_margin);
        if (messageListItem.isTheirs()) {
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMarginStart(marginStart);
            params.setMarginEnd(0);
            params.horizontalBias = 0f;
        } else {
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
            iv_reply.setBackgroundResource(R.drawable.replies);
            params.horizontalBias = 0f;
            paramsText.endToEnd = cl_reply.getId();
            paramsArrow.startToStart = cl_reply.getId();
            paramsText.startToEnd = iv_reply.getId();
        } else {
            iv_reply.setBackgroundResource(R.drawable.repliesout);
            params.horizontalBias = 1f;
            paramsArrow.endToEnd = cl_reply.getId();
            paramsText.startToStart = cl_reply.getId();
            paramsArrow.startToEnd = tv_reply.getId();
        }
        cl_reply.setLayoutParams(params);
        iv_reply.setLayoutParams(paramsArrow);
        tv_reply.setLayoutParams(paramsText);
    }

    public void configParamsReadState(){
        if (read_state.getVisibility() != View.VISIBLE) return;

        ConstraintSet set = new ConstraintSet();
        set.clone(cl_message);
        set.clear(R.id.read_state, ConstraintSet.START);
        set.clear(R.id.read_state, ConstraintSet.END);
        set.applyTo(cl_message);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) read_state.getLayoutParams();

        if (this.message.getAttachments() == null || this.message.getAttachments().isEmpty()) {
            if (messageListItem.isMine()){
                params.endToStart = alv_attachments.getId();
            }else{
                params.startToEnd = alv_attachments.getId();
            }
        }else {
            if (messageListItem.isMine()){
                params.endToStart = tv_text.getId();
            }else{
                params.startToEnd = tv_text.getId();
            }
        }
        read_state.setLayoutParams(params);
    }

    public void setViewHolderFactory(MessageViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }


}
