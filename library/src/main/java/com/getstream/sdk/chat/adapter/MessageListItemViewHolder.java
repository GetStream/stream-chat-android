package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.MessageBubbleDrawable;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.ext.latex.JLatexMathPlugin;
import ru.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import ru.noties.markwon.image.ImagesPlugin;


public class MessageListItemViewHolder extends BaseMessageListItemViewHolder {
    final String TAG = MessageListItemViewHolder.class.getSimpleName();

    private ConstraintLayout cl_message, headerView;
    private TextView tv_text, tv_deleted;
    private RecyclerView rv_reaction;

    private LinearLayout ll_send_failed;
    private TextView tv_failed_text, tv_failed_des;
    private ImageView cv_avatar;
    private ImageView iv_docket;

    private TextView tv_gap_header, tv_gap_sameUser, tv_gap_reaction, tv_gap_media_file, tv_gap_attach;
    private TextView tv_username, tv_messagedate, tv_initials;

    // Delivered Indicator
    private View view_read_indicator;
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
    private int position;
    private boolean isThread;
    private boolean isThreadHeader = false;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private Context context;
    private Message message;
    private Entity entity;
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

        cv_avatar = itemView.findViewById(R.id.cv_avatar);
        tv_initials = itemView.findViewById(R.id.tv_initials);

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


        view_read_indicator = itemView.findViewById(R.id.view_read_indicator);

        tv_indicator_initials = itemView.findViewById(R.id.tv_indicator_initials);
        tv_read_count = itemView.findViewById(R.id.tv_read_count);
        cv_indicator_avatar = itemView.findViewById(R.id.cv_indicator_avatar);
        pb_indicator = itemView.findViewById(R.id.pb_indicator);

        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
    }

    @Override
    public void bind(Context context, ChannelState channelState, Entity entity, int position, boolean isThread, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        // set binding
        this.context = context;
        this.channelState = channelState;
        this.position = position;
        this.isThread = isThread;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.entity = entity;
        this.message = entity.getMessage();
        this.positions = entity.getPositions();



        if (this.message.getAttachments() == null || this.message.getAttachments().size() == 0) {
            alv_attachments.setVisibility(View.GONE);
        } else {
            alv_attachments.setVisibility(View.VISIBLE);
            alv_attachments.setViewHolderFactory(viewHolderFactory);
            alv_attachments.setStyle(style);
            alv_attachments.setMessage(this.message);
        }

        // apply the style based on mine or theirs
        if (entity.isMine()) {
            this.applyStyleMine();
        } else {
            this.applyStyleTheirs();
        }

        // apply position related style tweaks
        this.applyPositionsStyle();


        isThreadHeader = (isThread && this.clickListener == null && longClickListener == null);


        // TODO: hook up click and longclick

        // Configure UIs
        configSendFailed();
        configMessageText();
        // TODO: configDelieveredIndicator();
        configReactionView();
        configReplyView();

        // Configure Laytout Params

        configParamsMessageText();
        configParamsDeletedMessage();
        configParamsUserAvatar();
        configParamsUserInitials();
        configParamsReactionRecycleView();
        configPramsDeliveredIndicator();
        configParamsReactionTail();
        configParamsMessageDate();
        configParamsReply();
//        configParamsAttachment();
    }
    // endregion
    // TODO: configure the deliver indicator

    private void applyStyleMine() {
        Drawable background = style.getMessageBubbleDrawableMine();
        tv_text.setBackground(background);
        tv_text.setTextColor(style.getMessageTextColorMine());
    }

    private void applyStyleTheirs() {
        Drawable background = style.getMessageBubbleDrawableTheirs();

        InputStream resource = context.getResources().openRawResource(R.raw.typing);
        Bitmap bitmap = BitmapFactory.decodeStream(resource);
        tv_text.setBackground(new MessageBubbleDrawable(R.color.stream_gray_dark, 1, Color.GREEN));


        //tv_text.setBackground(background);
        tv_text.setTextColor(style.getMessageTextColorTheirs());
    }

    private void applyPositionsStyle() {
        // TOP position has a rounded top left corner and extra spacing
        // BOTTOM position shows the user avatar & message time

        // RESET the defaults
        tv_username.setVisibility(View.GONE);
        tv_initials.setVisibility(View.GONE);
        tv_messagedate.setVisibility(View.GONE);
        cv_avatar.setVisibility(View.GONE);
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
            tv_initials.setVisibility(View.VISIBLE);
            tv_messagedate.setVisibility(View.VISIBLE);
            cv_avatar.setVisibility(View.VISIBLE);
            if (entity.isTheirs()) {
                tv_username.setVisibility(View.VISIBLE);
                tv_username.setText(message.getUser().getName());
            } else
                tv_username.setVisibility(View.GONE);

            tv_initials.setText(message.getUser().getInitials());
            if (message.getDate() == null) message.setStartDay(Arrays.asList(message), null);
            if (message.getDate().equals("Today") || message.getDate().equals("Yesterday"))
                tv_messagedate.setText(message.getTime());
            else
                tv_messagedate.setText(message.getDate() + ", " + message.getTime());

            String avatarImageUrl = message.getUser().getImage();
            if (!TextUtils.isEmpty(avatarImageUrl)) {
                if (StringUtility.isValidImageUrl(avatarImageUrl))
                    Utils.circleImageLoad(cv_avatar, avatarImageUrl);
                else {
                    cv_avatar.setVisibility(View.GONE);
                }
            } else {
                cv_avatar.setVisibility(View.GONE);
            }
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
                if (clickListener != null) {
                    String tag = TextUtils.isEmpty(message.getCommand()) ? Constant.TAG_MESSAGE_RESEND : Constant.TAG_MESSAGE_INVALID_COMMAND;
                    v.setTag(new MessageTagModel(tag, position));
                    clickListener.onClick(v);
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
                    .usePlugin(ImagesPlugin.create(context))
                    .usePlugin(CorePlugin.create())
                    .usePlugin(StrikethroughPlugin.create())
                    .usePlugin(JLatexMathPlugin.create(tv_text.getTextSize()))
                    .build();
        markwon.setMarkdown(tv_text, Global.getMentionedText(message));



        // Set Click Listener
        tv_text.setOnClickListener((View v) -> {
            Log.d(TAG, "onClick: " + position);
            if (clickListener != null) {
                v.setTag(new MessageTagModel(Constant.TAG_MESSAGE_REACTION, position));
                clickListener.onClick(v);
            }
        });

        tv_text.setOnLongClickListener((View v) -> {
            Log.d(TAG, "Long onClick: " + position);
            if (longClickListener != null) {
                v.setTag(String.valueOf(position));
                longClickListener.onLongClick(v);
            }
            return true;
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
        if (entity.isTheirs())
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
            if (clickListener != null) {
                v.setTag(new MessageTagModel(Constant.TAG_MOREACTION_REPLY, position));
                clickListener.onClick(v);
            }
        });
    }





    // endregion

    // region Layout Params
    private void configParamsMessageText() {
        if (tv_text.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_text.getLayoutParams();
        if (entity.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_text.setLayoutParams(params);
    }

    private void configParamsDeletedMessage() {
        if (tv_deleted.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_deleted.getLayoutParams();
        if (entity.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_deleted.setLayoutParams(params);
    }

    private void configParamsMessageDate() {
        if (tv_messagedate.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_messagedate.getLayoutParams();
        if (entity.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_messagedate.setLayoutParams(params);
    }

    private void configParamsReactionRecycleView() {
        if (rv_reaction.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) rv_reaction.getLayoutParams();
        if (entity.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        rv_reaction.setLayoutParams(params);
    }



    private void configPramsDeliveredIndicator() {
        if (view_read_indicator.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view_read_indicator.getLayoutParams();
        if (tv_text.getVisibility() == View.VISIBLE) {
            params.bottomToBottom = tv_text.getId();
            params.endToStart = tv_text.getId();
        } else {
            //params.bottomToBottom = cl_attachment.getId();
            //params.endToStart = cl_attachment.getId();
        }
        view_read_indicator.setLayoutParams(params);
    }

    private void configParamsReactionTail() {
        if (iv_docket.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) iv_docket.getLayoutParams();
        if (entity.isTheirs()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        iv_docket.setLayoutParams(params);
    }

    private void configParamsUserAvatar() {
        if (cv_avatar.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cv_avatar.getLayoutParams();
        int marginStart = (int) context.getResources().getDimension(R.dimen.message_avatar_margin);
        if (entity.isTheirs()) {
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMarginStart(marginStart);
            params.setMarginEnd(0);
            params.horizontalBias = 0f;
        } else {
            params.setMarginStart(0);
            params.setMarginEnd(marginStart);
            params.horizontalBias = 1f;
        }
        cv_avatar.setLayoutParams(params);
    }

    private void configParamsUserInitials() {
        if (tv_initials.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_initials.getLayoutParams();
        int marginStart = (int) context.getResources().getDimension(R.dimen.message_avatar_margin);
        if (entity.isTheirs()) {
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMarginStart(marginStart);
            params.setMarginEnd(0);
            params.horizontalBias = 0f;
        } else {
            params.setMarginStart(0);
            params.setMarginEnd(marginStart);
            params.horizontalBias = 1f;
        }
        tv_initials.setLayoutParams(params);
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
        if (entity.isTheirs()) {
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

    public void setViewHolderFactory(MessageViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }
}
