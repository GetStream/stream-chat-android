package com.getstream.sdk.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;

import top.defaults.drawabletoolbox.DrawableBuilder;

public class DefaultBubbleHelper {
    private static int topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius;
    private static int bgColor, strokeColor, strokeWidth;

    public static MessageListView.BubbleHelper initDefaultBubbleHelper(MessageListViewStyle style, Context context) {
        return new MessageListView.BubbleHelper() {
            @Override
            public Drawable getDrawableForMessage(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions) {
                if (mine) {
                    if (style.getMessageBubbleDrawableMine() != null)
                        return style.getMessageBubbleDrawableMine();

                    configParamsMine(style);

                    // set background for Failed or Error message
                    if (message.getSyncStatus() == Sync.LOCAL_FAILED
                            || message.getType().equals(ModelType.message_error))
                        bgColor = context.getResources().getColor(R.color.stream_message_failed);

                    if (isDefaultBubble(style, context))
                        applyStyleDefaultMine(positions, context);
                } else {
                    if (style.getMessageBubbleDrawableTheirs() != null)
                        return style.getMessageBubbleDrawableTheirs();

                    configParamsTheirs(style);
                    if (isDefaultBubble(style, context))
                        applyStyleDefaultTheirs(positions, context);
                }
                return getBubbleDrawable();
            }

            @Override
            public Drawable getDrawableForAttachment(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions, Attachment attachment) {
                if (attachment == null
                        || attachment.getType().equals(ModelType.attach_unknown))
                    return null;

                if (mine) {
                    if (style.getMessageBubbleDrawableMine() != null)
                        return style.getMessageBubbleDrawableMine();

                    configParamsMine(style);
                    if (isDefaultBubble(style, context)) {
                        applyStyleDefaultMine(positions, context);

                        // set corner radius if the attachment has title or description
                        if (!TextUtils.isEmpty(attachment.getTitle()) && !attachment.getType().equals(ModelType.attach_file))
                            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                    }
                } else {
                    if (style.getMessageBubbleDrawableTheirs() != null)
                        return style.getMessageBubbleDrawableTheirs();

                    configParamsTheirs(style);

                    if (isDefaultBubble(style, context)) {
                        applyStyleDefaultTheirs(positions, context);
                        // set corner radius if the attachment has title or description
                        if (!TextUtils.isEmpty(attachment.getTitle()) && !attachment.getType().equals(ModelType.attach_file))
                            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                    }
                }
                return getBubbleDrawable();
            }

            @Override
            public Drawable getDrawableForAttachmentDescription(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions){
                if (mine) {
                    configParamsMine(style);
                    if (isDefaultBubble(style, context))
                        applyStyleDefaultMine(positions, context);
                } else {
                    if (style.getMessageBubbleDrawableTheirs() != null)
                        return style.getMessageBubbleDrawableTheirs();
                    configParamsTheirs(style);
                    if (isDefaultBubble(style, context))
                        applyStyleDefaultTheirs(positions, context);
                }
                topLeftRadius = 0;
                topRightRadius = 0;
                return getBubbleDrawable();
            }
        };
    }

    private static void configParamsMine(MessageListViewStyle style){
        bgColor = style.getMessageBackgroundColorMine();
        strokeColor = style.getMessageBorderColorMine();
        strokeWidth = style.getMessageBorderWidthMine();
        topLeftRadius = style.getMessageTopLeftCornerRadiusMine();
        topRightRadius = style.getMessageTopRightCornerRadiusMine();
        bottomRightRadius = style.getMessageBottomRightCornerRadiusMine();
        bottomLeftRadius = style.getMessageBottomLeftCornerRadiusMine();
    }

    private static void configParamsTheirs(MessageListViewStyle style){
        bgColor = style.getMessageBackgroundColorTheirs();
        strokeColor = style.getMessageBorderColorTheirs();
        strokeWidth = style.getMessageBorderWidthTheirs();
        topLeftRadius = style.getMessageTopLeftCornerRadiusTheirs();
        topRightRadius = style.getMessageTopRightCornerRadiusTheirs();
        bottomRightRadius = style.getMessageBottomRightCornerRadiusTheirs();
        bottomLeftRadius = style.getMessageBottomLeftCornerRadiusTheirs();
    }

    private static void applyStyleDefaultMine(List<MessageViewHolderFactory.Position> positions, Context context){
        topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
        bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
            topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
        } else {
            topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
        }
    }

    private static void applyStyleDefaultTheirs(List<MessageViewHolderFactory.Position> positions, Context context){
        topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
        bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
            topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
        } else {
            topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
        }
    }

    private static boolean isDefaultBubble(MessageListViewStyle style, Context context) {
        return (style.getMessageTopLeftCornerRadiusMine() == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageTopRightCornerRadiusMine() == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageBottomRightCornerRadiusMine() == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2)) &&
                (style.getMessageBottomLeftCornerRadiusMine() == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageTopLeftCornerRadiusTheirs() == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageTopRightCornerRadiusTheirs() == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageBottomRightCornerRadiusTheirs() == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageBottomLeftCornerRadiusTheirs() == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2));
    }

    private static Drawable getBubbleDrawable(){
        return new DrawableBuilder()
                .rectangle()
                .strokeColor(strokeColor)
                .strokeWidth(strokeWidth)
                .solidColor(bgColor)
                .cornerRadii(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)
                .build();
    }
}
