package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Message;
import com.getstream.sdk.chat.model.MessageTagModel;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.model.SelectAttachmentModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.ext.latex.JLatexMathPlugin;
import ru.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import ru.noties.markwon.image.ImagesPlugin;

public class MessageListItemViewHolder extends RecyclerView.ViewHolder {
    // region LifeCycle
    final String TAG = MessageListItemViewHolder.class.getSimpleName();

    private ConstraintLayout cl_message, headerView;
    private TextView tv_text, tv_deleted;
    private RecyclerView rv_reaction;
    private LinearLayout ll_typingusers;
    private LinearLayout ll_send_failed;
    private TextView tv_failed_text, tv_failed_des;
    private ImageView cv_avatar;
    private ImageView iv_docket;
    private TextView tv_header_date, tv_header_time;
    private TextView tv_gap_header, tv_gap_sameUser, tv_gap_reaction, tv_gap_media_file, tv_gap_attach;
    private TextView tv_username, tv_messagedate, tv_initials;
    // Attachment
    private ConstraintLayout cl_attachment, cl_attachment_media;
    private PorterShapeImageView iv_media_thumb;
    private ListView lv_attachment_file;
    private ImageView iv_media_more;
    private TextView tv_more;
    private TextView tv_media_title, tv_media_play, tv_media_des;
    private ImageView iv_command_title;
    // Action
    private ConstraintLayout cl_action;
    private TextView tv_action_send, tv_action_shuffle, tv_action_cancel;
    // Delivered Indicator
    private View view_read_indicator;
    private TextView tv_indicator_initials, tv_read_count;
    private ImageView cv_indicator_avatar;
    private ProgressBar pb_indicator;
    // Replay
    private ConstraintLayout cl_reply;
    private ImageView iv_reply;
    private TextView tv_reply;
    // Tying
    private ImageView iv_typing_indicator;

    private Markwon markwon;
    private RecyclerView.LayoutManager mLayoutManager;

    private ChannelResponse channelResponse;
    private List<Message> messageList;
    private int position;
    private boolean isThread;
    private boolean isThreadHeader = false;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
    private Context context;
    private Message message;

    public MessageListItemViewHolder(View itemView) {
        super(itemView);
        cl_message = itemView.findViewById(R.id.cl_message);
        ll_typingusers = itemView.findViewById(R.id.ll_typing_indicator);

        headerView = itemView.findViewById(R.id.cl_header);
        if (headerView == null) Log.d(TAG, "headerView null");

        tv_header_date = itemView.findViewById(R.id.tv_header_date);
        tv_header_time = itemView.findViewById(R.id.tv_header_time);

        rv_reaction = itemView.findViewById(R.id.rv_reaction);
        iv_docket = itemView.findViewById(R.id.iv_docket);
        // Attach
        cl_attachment = itemView.findViewById(R.id.cl_attachment);
        cl_attachment_media = itemView.findViewById(R.id.cl_attachment_media);
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
        lv_attachment_file = itemView.findViewById(R.id.lv_attachment_file);
        iv_media_more = itemView.findViewById(R.id.iv_media_more);
        tv_more = itemView.findViewById(R.id.tv_more);
        tv_media_title = itemView.findViewById(R.id.tv_media_title);
        tv_media_play = itemView.findViewById(R.id.tv_media_play);
        tv_media_des = itemView.findViewById(R.id.tv_media_des);
        iv_command_title = itemView.findViewById(R.id.iv_command_title);

        cl_action = itemView.findViewById(R.id.cl_action);
        tv_action_send = itemView.findViewById(R.id.tv_action_send);
        tv_action_shuffle = itemView.findViewById(R.id.tv_action_shuffle);
        tv_action_cancel = itemView.findViewById(R.id.tv_action_cancel);

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

        iv_typing_indicator = itemView.findViewById(R.id.iv_typing_indicator);
        view_read_indicator = itemView.findViewById(R.id.view_read_indicator);

        tv_indicator_initials = itemView.findViewById(R.id.tv_indicator_initials);
        tv_read_count = itemView.findViewById(R.id.tv_read_count);
        cv_indicator_avatar = itemView.findViewById(R.id.cv_indicator_avatar);
        pb_indicator = itemView.findViewById(R.id.pb_indicator);

        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
    }

    public void bind(Context context, ChannelResponse channelResponse, @NonNull List<Message> messageList, int position, boolean isThread, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        // set binding
        this.context = context;
        this.channelResponse = channelResponse;
        this.messageList = messageList;
        this.position = position;
        this.isThread = isThread;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        if (position < messageList.size())
            this.message = messageList.get(position);

        isThreadHeader = (isThread && clickListener == null && longClickListener == null);

        // Configure UIs

        if (position == messageList.size()) {
            configTypingIndicator();
            return;
        } else {
            cl_message.setVisibility(View.VISIBLE);
            ll_typingusers.setVisibility(View.GONE);
            iv_typing_indicator.setVisibility(View.GONE);
        }
        configHeaderView();
        configUserInfo();
        configSendFailed();
        configMessageText();
        configAttachment();
        configDelieveredIndicator();
        configCommand();
        configAction();
        configReactionView();
        configReplyView();
        configGaps();

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

    // region Config UIs
    private void configTypingIndicator() {
        cl_message.setVisibility(View.GONE);
        if (Global.typingUsers.size() > 0) {
            ll_typingusers.setVisibility(View.VISIBLE);
            iv_typing_indicator.setVisibility(View.VISIBLE);
            createTypingUsersView();
            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv_typing_indicator);
            Glide.with(context).load(R.raw.typing).into(imageViewTarget);
        } else {
            ll_typingusers.setVisibility(View.INVISIBLE);
            iv_typing_indicator.setVisibility(View.INVISIBLE);
        }
    }

    private void configDelieveredIndicator() {
        if (position == messageList.size() - 1 && !message.isIncoming() && !isThread) {
            view_read_indicator.setVisibility(View.VISIBLE);
            List<User> readUsers = Global.getReadUsers(channelResponse, message);
            if (readUsers == null || !TextUtils.isEmpty(message.getDeleted_at()) || message.getType().equals(ModelType.message_error)) {
                view_read_indicator.setVisibility(View.GONE);
                Log.d(TAG, "Deliever Indicator 1");
                return;
            }
            if (readUsers.size() > 0) {
                pb_indicator.setVisibility(View.GONE);
                cv_indicator_avatar.setVisibility(View.VISIBLE);
                tv_indicator_initials.setVisibility(View.VISIBLE);
                cv_indicator_avatar.setBackgroundResource(0);
                Utils.circleImageLoad(cv_indicator_avatar, channelResponse.getLastReadUser().getImage());
                tv_indicator_initials.setText(channelResponse.getLastReadUser().getUserInitials());

                if (readUsers.size() > 1) {
                    tv_read_count.setVisibility(View.VISIBLE);
                    tv_read_count.setText(String.valueOf(readUsers.size() - 1));
                } else {
                    tv_read_count.setVisibility(View.GONE);
                }
                Log.d(TAG, "Deliever Indicator 2");
            } else {
                if (message.isDelivered()) {
                    cv_indicator_avatar.setVisibility(View.VISIBLE);
                    tv_indicator_initials.setVisibility(View.GONE);
                    tv_read_count.setVisibility(View.GONE);
                    pb_indicator.setVisibility(View.GONE);
                    cv_indicator_avatar.setBackgroundResource(R.drawable.delivered_unseen);
                    Log.d(TAG, "Deliever Indicator 3");
                } else {
                    cv_indicator_avatar.setVisibility(View.GONE);
                    tv_indicator_initials.setVisibility(View.GONE);
                    tv_read_count.setVisibility(View.GONE);
                    pb_indicator.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Deliever Indicator 4");
                }
            }

            view_read_indicator.setOnClickListener((View v) -> {
                v.setTag(new MessageTagModel(Constant.TAG_MESSAGE_CHECK_DELIVERED, position));
                clickListener.onClick(v);
            });
        } else {
            view_read_indicator.setVisibility(View.GONE);
        }
    }

    private void createTypingUsersView() {
        ll_typingusers.removeAllViews();
        Resources resources = context.getResources();
        float marginLeft = resources.getDimension(R.dimen.user_avatar_margin_left);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < Global.typingUsers.size(); i++) {
            View v = vi.inflate(R.layout.view_user_avatar_initials, null);
            User user = Global.typingUsers.get(i);
            TextView textView = v.findViewById(R.id.tv_initials);
            ImageView imageView = v.findViewById(R.id.cv_avatar);
            textView.setText(user.getUserInitials());
            Utils.circleImageLoad(imageView, user.getImage());
            int height = (int) context.getResources().getDimension(R.dimen.message_typing_indicator_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(height, height);
            if (i == 0) {
                params.setMargins(0, 0, 0, 0);
            } else {
                params.setMargins(-(int) marginLeft, 0, 0, 0);
            }

            v.setLayoutParams(params);
            ll_typingusers.addView(v);
        }
    }

    private void configHeaderView() {
        headerView.setVisibility(headerViewVisible());
        if (headerView.getVisibility() != View.VISIBLE) return;

        String headerDate = message.getDate(), headerTime;
        if (message.isToday())
            headerDate = "Today";

        if (message.isYesterday())
            headerDate = "Yesterday";

        headerTime = message.getTime();
        tv_header_date.setText(headerDate);
        tv_header_time.setText(" AT " + headerTime);
    }

    private void configUserInfo() {
        if (messageTimeVisible()) {
            tv_username.setVisibility(View.VISIBLE);
            tv_initials.setVisibility(View.VISIBLE);
            tv_messagedate.setVisibility(View.VISIBLE);
            cv_avatar.setVisibility(View.VISIBLE);
            if (message.isIncoming()) {
                tv_username.setVisibility(View.VISIBLE);
                tv_username.setText(message.getUser().getName());
            } else
                tv_username.setVisibility(View.GONE);

            tv_initials.setText(message.getUser().getUserInitials());
            if (message.getDate() == null) Global.setStartDay(Arrays.asList(message), null);
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
        } else {
            tv_username.setVisibility(View.GONE);
            tv_initials.setVisibility(View.GONE);
            tv_messagedate.setVisibility(View.GONE);
            cv_avatar.setVisibility(View.GONE);
        }
    }

    private void configSendFailed() {
        if (message.getType().equals(ModelType.message_error)) {
            ll_send_failed.setVisibility(View.VISIBLE);
            tv_failed_text.setText(message.getText());
            int failedDes = TextUtils.isEmpty(message.getCommand()) ? R.string.message_failed_send : R.string.message_invalid_command;
            tv_failed_des.setText(context.getResources().getText(failedDes));
            int background = containerStyleOne(position) ? R.drawable.round_outgoing_failed1 : R.drawable.round_outgoing_failed2;
            ll_send_failed.setBackgroundResource(background);

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
        if (!TextUtils.isEmpty(message.getDeleted_at())) {
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

        // Set Text background
        if (StringUtility.isEmoji(message.getText()))
            tv_text.setBackgroundResource(0); // Check Emoji Text
        else {
            int background;
            if (containerStyleOne(position)) {
                if (message.isIncoming())
                    background = (message.getAttachments() == null || message.getAttachments().isEmpty()) ? R.drawable.round_incoming_text1 : R.drawable.round_incoming_text2;
                else {
                    background = (message.getAttachments() == null || message.getAttachments().isEmpty()) ? R.drawable.round_outgoing_text1 : R.drawable.round_outgoing_text2;
                }

            } else {
                if (message.isIncoming())
                    background = R.drawable.round_incoming_text2;
                else
                    background = R.drawable.round_outgoing_text2;
            }
            tv_text.setBackgroundResource(background);
        }
        // Set Color
        if (message.isIncoming()) {
            tv_text.setTextColor(context.getResources().getColor(R.color.message_text_incoming));
        } else {
            tv_text.setTextColor(context.getResources().getColor(R.color.message_text_outgoing));
        }

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

    private void configAttachment() {
        if (tv_deleted.getVisibility() == View.VISIBLE || ll_send_failed.getVisibility() == View.VISIBLE) {
            cl_attachment.setVisibility(View.GONE);
            return;
        }
        if (message.getAttachments() == null) {
            cl_attachment.setVisibility(View.GONE);
            return;
        }
        if (message.getAttachments().size() == 0) {
            cl_attachment.setVisibility(View.GONE);
            return;
        }

        cl_attachment.setVisibility(View.VISIBLE);

        boolean hasFile = false;
        boolean hasMedia = false;
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType() == null) continue;
            if (attachment.getType().equals(ModelType.attach_file)) {
                hasFile = true;
            } else {
                hasMedia = true;
            }
        }

        if (hasMedia) {
            cl_attachment_media.setVisibility(View.VISIBLE);
            configMediaAttach();
        } else {
            cl_attachment_media.setVisibility(View.GONE);
        }

        if (hasFile) {
            lv_attachment_file.setVisibility(View.VISIBLE);
            configFileAttach();
        } else {
            lv_attachment_file.setVisibility(View.GONE);
        }
    }

    private void configCommand() {
        if (!TextUtils.isEmpty(message.getCommand())) {
            int back;
            iv_command_title.setVisibility(View.VISIBLE);
            switch (message.getCommand()) {
                case Constant.COMMAND_GIPHY:
                    back = R.drawable.logogiphy;
                    break;
                case Constant.COMMAND_IMGUR:
                    back = R.drawable.logogiphy;
                    break;
                case Constant.COMMAND_BAN:
                    back = R.drawable.logogiphy;
                    break;
                case Constant.COMMAND_FLAG:
                    back = R.drawable.logogiphy;
                    break;
                default:
                    back = 0;
                    break;
            }
            iv_command_title.setBackgroundResource(back);
        } else {
            iv_command_title.setVisibility(View.GONE);
        }
    }

    private void configAction() {
        if (message.getType().equals(ModelType.message_ephemeral)) {
            cl_action.setVisibility(View.VISIBLE);
            tv_action_send.setOnClickListener((View v) -> {
                v.setTag(new MessageTagModel(Constant.TAG_ACTION_SEND, position));
                clickListener.onClick(v);
            });
            if (tv_action_shuffle.getTag() == null) {
                tv_action_shuffle.setBackgroundResource(R.drawable.round_action_shuffle);
            } else {
                tv_action_shuffle.setBackgroundResource(R.drawable.round_action_shuffle_select);
            }

            tv_action_shuffle.setOnClickListener((View v) -> {
                tv_action_shuffle.setBackgroundResource(R.drawable.round_action_shuffle_select);
                v.setTag(new MessageTagModel(Constant.TAG_ACTION_SHUFFLE, position));
                clickListener.onClick(v);
            });
            tv_action_cancel.setOnClickListener((View v) -> {
                v.setTag(new MessageTagModel(Constant.TAG_ACTION_CANCEL, position));
                clickListener.onClick(v);
            });
        } else {
            cl_action.setVisibility(View.GONE);
        }
    }

    private void configMediaAttach() {
        List<Attachment> attachments = new ArrayList<>();
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType() == null) continue;
            if (!attachment.getType().equals(ModelType.attach_file)) {
                attachments.add(attachment);
            }
        }

        final String type = attachments.get(0).getType();

        String attachUrl = attachments.get(0).getImageURL();
        if (attachments.get(0).getType().equals(ModelType.attach_image)) {
            attachUrl = attachments.get(0).getImageURL();
        } else if (attachments.get(0).getType().equals(ModelType.attach_giphy)) {
            attachUrl = attachments.get(0).getThumbURL();
        } else if (attachments.get(0).getType().equals(ModelType.attach_video)) {
            attachUrl = attachments.get(0).getThumbURL();
        } else {
            if (attachUrl == null) attachUrl = attachments.get(0).getImage();
        }
        if (TextUtils.isEmpty(attachUrl)) {
            cl_attachment_media.setVisibility(View.GONE);
            return;
        }
        cl_attachment_media.setVisibility(View.VISIBLE);
        configAttachViewBackground(cl_attachment_media);
        configImageThumbBackground(attachments.get(0));
        // More
        if (attachments.size() > 1) {
            iv_media_more.setVisibility(View.VISIBLE);
            tv_more.setText(attachments.size() - 1 + " more");
        } else {
            iv_media_more.setVisibility(View.GONE);
        }
        tv_more.setVisibility(iv_media_more.getVisibility());


        // Set Click Listener
        cl_attachment_media.setOnClickListener((View v) -> {
            Log.d(TAG, "onClick Attach : " + position);
            if (clickListener != null) {
                if (attachments.size() > 0) {
                    SelectAttachmentModel attachmentModel = new SelectAttachmentModel();
                    attachmentModel.setAttachmentIndex(0);
                    attachmentModel.setAttachments(attachments);
                    Log.d(TAG, "Attachments set : " + attachmentModel.getAttachments().size());
                    v.setTag(attachmentModel);
                }
                clickListener.onClick(v);
            }

        });


        cl_attachment_media.setOnLongClickListener((View v) -> {
            Log.d(TAG, "Long onClick Attach: " + position);
            if (longClickListener != null) {
                v.setTag(String.valueOf(position));
                longClickListener.onLongClick(v);
            }
            return true;
        });
        if (!attachUrl.contains("https:"))
            attachUrl = "https:" + attachUrl;
        Glide.with(context)
                .load(attachUrl)
                .into(iv_media_thumb);

        tv_media_title.setText(attachments.get(0).getTitle());
        tv_media_des.setText(attachments.get(0).getText());

        if (StringUtility.isNullOrEmpty(attachments.get(0).getText()))
            tv_media_des.setVisibility(View.GONE);
        else
            tv_media_des.setVisibility(View.VISIBLE);

        if (StringUtility.isNullOrEmpty(attachments.get(0).getTitle()))
            tv_media_title.setVisibility(View.GONE);
        else
            tv_media_title.setVisibility(View.VISIBLE);

        if (type.equals(ModelType.attach_video))
            tv_media_play.setVisibility(View.VISIBLE);
        else
            tv_media_play.setVisibility(View.GONE);
    }

    private void configAttachViewBackground(View view) {
        int background;
        if (message.isIncoming()) {
            if (containerStyleOne(position)) {
                if (view.equals(lv_attachment_file)) {
                    if (cl_attachment_media.getVisibility() == View.VISIBLE) {
                        background = R.drawable.round_incoming_text2;
                    } else {
                        background = R.drawable.round_incoming_text1;
                    }
                } else
                    background = R.drawable.round_incoming_text1;
            } else {
                background = R.drawable.round_incoming_text2;
            }
        } else {
            if (containerStyleOne(position)) {
                if (view.equals(lv_attachment_file)) {
                    if (cl_attachment_media.getVisibility() == View.VISIBLE) {
                        background = R.drawable.round_outgoing_text2;
                    } else {
                        background = R.drawable.round_outgoing_text1;
                    }
                } else
                    background = R.drawable.round_outgoing_text1;
            } else {
                background = R.drawable.round_outgoing_text2;
            }
        }
        view.setBackgroundResource(background);
    }

    private void configImageThumbBackground(Attachment attachment) {
        int mediaBack, moreBack;
        if (message.isIncoming()) {
            if (!TextUtils.isEmpty(attachment.getText()) ||
                    !TextUtils.isEmpty(attachment.getTitle())) {
                if (containerStyleOne(position)) {
                    mediaBack = R.drawable.round_attach_media;
                    moreBack = R.drawable.round_attach_more;
                } else {
                    mediaBack = R.drawable.round_attach_media_incoming3;
                    moreBack = R.drawable.round_attach_more_incoming3;
                }
            } else {
                if (containerStyleOne(position)) {
                    mediaBack = R.drawable.round_attach_media_incoming1;
                    moreBack = R.drawable.round_attach_more_incoming1;
                } else {
                    mediaBack = R.drawable.round_attach_media_incoming2;
                    moreBack = R.drawable.round_attach_more_incoming2;
                }
            }
        } else {
            if (!TextUtils.isEmpty(attachment.getText()) ||
                    !TextUtils.isEmpty(attachment.getTitle())) {
                if (containerStyleOne(position)) {
                    mediaBack = R.drawable.round_attach_media;
                    moreBack = R.drawable.round_attach_more;
                } else {
                    mediaBack = R.drawable.round_attach_media_outgoing3;
                    moreBack = R.drawable.round_attach_more_outgoing3;
                }
            } else {
                if (containerStyleOne(position)) {
                    mediaBack = R.drawable.round_attach_media_outgoing1;
                    moreBack = R.drawable.round_attach_more_outgoing1;
                } else {
                    mediaBack = R.drawable.round_attach_media_outgoing2;
                    moreBack = R.drawable.round_attach_more_outgoing2;
                }
            }
        }
        if (iv_media_more.getVisibility() == View.VISIBLE)
            iv_media_more.setBackgroundResource(moreBack);

        iv_media_thumb.setShape(context, context.getResources().getDrawable(mediaBack));
    }

    private void configFileAttach() {
        configAttachViewBackground(lv_attachment_file);
        List<Attachment> attachments = new ArrayList<>();
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType() == null) continue;
            if (attachment.getType().equals(ModelType.attach_file)) {
                attachments.add(attachment);
            }
        }
        AttachmentListAdapter attachAdapter = new AttachmentListAdapter(context, attachments, false, false);

        lv_attachment_file.setAdapter(attachAdapter);
        lv_attachment_file.setOnItemClickListener((AdapterView<?> parent, View view,
                                                   int position, long id) -> {
            Log.d(TAG, "Attach onClick: " + position);
            if (clickListener != null) {

                SelectAttachmentModel attachmentModel = new SelectAttachmentModel();
                attachmentModel.setAttachmentIndex(position);
                attachmentModel.setAttachments(attachments);

                view.setTag(attachmentModel);
                clickListener.onClick(view);
            }
        });
        lv_attachment_file.setOnItemLongClickListener((AdapterView<?> parent, View view, int position_, long id) -> {
            if (longClickListener != null) {
                view.setTag(position);
                longClickListener.onLongClick(view);
            }
            return true;
        });

        float height = context.getResources().getDimension(R.dimen.attach_file_height);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) lv_attachment_file.getLayoutParams();
        params.height = (int) height * attachments.size();
        lv_attachment_file.setLayoutParams(params);
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
        if (message.isIncoming())
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

    private void configGaps() {
        // Header Gap
        tv_gap_header.setVisibility(messageGapViewVisible(position));

        // Same User Gap
        if (containerStyleOne(position))
            tv_gap_sameUser.setVisibility(View.GONE);
        else
            tv_gap_sameUser.setVisibility(View.VISIBLE);
        // Media_File Gap
        if (cl_attachment_media.getVisibility() == View.VISIBLE && lv_attachment_file.getVisibility() == View.VISIBLE)
            tv_gap_media_file.setVisibility(View.VISIBLE);
        else
            tv_gap_media_file.setVisibility(View.GONE);

        // Attach Gap
        tv_gap_attach.setVisibility(cl_attachment.getVisibility());
        if (cl_attachment.getVisibility() == View.VISIBLE && TextUtils.isEmpty(message.getText()))
            tv_gap_attach.setVisibility(View.GONE);

        // Reaction Gap
        tv_gap_reaction.setVisibility(rv_reaction.getVisibility());

        // ONLY_FOR_DEBUG
        if (Global.checkMesageGapState) {
            tv_gap_header.setBackgroundResource(R.color.gap_header);
            tv_gap_sameUser.setBackgroundResource(R.color.gap_message);
            tv_gap_media_file.setBackgroundResource(R.color.gap_media_file);
            tv_gap_reaction.setBackgroundResource(R.color.gap_reaction);
            tv_gap_attach.setBackgroundResource(R.color.gap_attach);
        } else {
            tv_gap_header.setBackgroundResource(0);
            tv_gap_sameUser.setBackgroundResource(0);
            tv_gap_media_file.setBackgroundResource(0);
            tv_gap_reaction.setBackgroundResource(0);
            tv_gap_attach.setBackgroundResource(0);
        }
    }

    private int headerViewVisible() {
        if (isThreadHeader) return View.GONE;
//        if (position == 0) return View.VISIBLE;
        if (this.message.isStartDay()) return View.VISIBLE;
        return View.GONE;
    }

    private boolean messageTimeVisible() {
        if (isThreadHeader) return true;
        if (position == messageList.size() - 1) return true;
        Message nextMessage;
        if (position < messageList.size() - 1) {
            nextMessage = messageList.get(position + 1);
            if (nextMessage.isStartDay()) return true;
            if (message.getUser().getId().equals(nextMessage.getUser().getId())) return false;
        }
        return true;
    }

    private boolean containerStyleOne(int position) {
        Message beforeMessage = null;
        if (headerView.getVisibility() == View.VISIBLE) return true;
        try {
            beforeMessage = messageList.get(position - 1);
            return !(message.getUser().getId().equals(beforeMessage.getUser().getId()));
        } catch (Exception e) {
        }
        return true;
    }

    private int messageGapViewVisible(int position) {
        if (!containerStyleOne(position)) return View.GONE;
        return View.VISIBLE;
    }
    // endregion

    // region Layout Params
    private void configParamsMessageText() {
        if (tv_text.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_text.getLayoutParams();
        if (message.isIncoming()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_text.setLayoutParams(params);
    }

    private void configParamsDeletedMessage() {
        if (tv_deleted.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_deleted.getLayoutParams();
        if (message.isIncoming()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_deleted.setLayoutParams(params);
    }

    private void configParamsMessageDate() {
        if (tv_messagedate.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tv_messagedate.getLayoutParams();
        if (message.isIncoming()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        tv_messagedate.setLayoutParams(params);
    }

    private void configParamsReactionRecycleView() {
        if (rv_reaction.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) rv_reaction.getLayoutParams();
        if (message.isIncoming()) {
            params.horizontalBias = 0f;
        } else {
            params.horizontalBias = 1f;
        }
        rv_reaction.setLayoutParams(params);
    }

    private void configParamsAttachment() {
        if (cl_attachment.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cl_attachment.getLayoutParams();
        ConstraintLayout.LayoutParams paramsLv = (ConstraintLayout.LayoutParams) lv_attachment_file.getLayoutParams();
        if (lv_attachment_file.getVisibility() == View.VISIBLE) {
            if (message.isIncoming()) {
                params.horizontalBias = 0f;
                paramsLv.horizontalBias = 0f;
            } else {
                params.horizontalBias = 1f;
                paramsLv.horizontalBias = 1f;
            }
            params.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            lv_attachment_file.setLayoutParams(paramsLv);
            cl_attachment.setLayoutParams(params);
        } else {
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        }
        cl_attachment.setLayoutParams(params);

        lv_attachment_file.setBackgroundColor(context.getResources().getColor(R.color.black));
    }

    private void configPramsDeliveredIndicator() {
        if (view_read_indicator.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view_read_indicator.getLayoutParams();
        if (tv_text.getVisibility() == View.VISIBLE) {
            params.bottomToBottom = tv_text.getId();
            params.endToStart = tv_text.getId();
        } else {
            params.bottomToBottom = cl_attachment.getId();
            params.endToStart = cl_attachment.getId();
        }
        view_read_indicator.setLayoutParams(params);
    }

    private void configParamsReactionTail() {
        if (iv_docket.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) iv_docket.getLayoutParams();
        if (message.isIncoming()) {
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
        if (message.isIncoming()) {
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
        if (message.isIncoming()) {
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
        if (message.isIncoming()) {
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
}
