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

                    applyStyleMine(style);

                    if (message.getSyncStatus() == Sync.LOCAL_FAILED
                            || message.getType().equals(ModelType.message_error))
                        bgColor = context.getResources().getColor(R.color.stream_message_failed);

                    if (isDefaultBubble(style, context)) {
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
                } else {
                    if (style.getMessageBubbleDrawableTheirs() != null)
                        return style.getMessageBubbleDrawableTheirs();

                    applyStyleTheirs(style);

                    if (isDefaultBubble(style, context)) {
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
                }
                return new DrawableBuilder()
                        .rectangle()
                        .strokeColor(strokeColor)
                        .strokeWidth(strokeWidth)
                        .solidColor(bgColor)
                        .cornerRadii(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)
                        .build();
            }

            @Override
            public Drawable getDrawableForAttachment(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions, Attachment attachment) {
                if (attachment == null
                        || attachment.getType().equals(ModelType.attach_unknown)
                        || attachment.getType().equals(ModelType.attach_file))
                    return null;

                if (mine) {
                    if (style.getMessageBubbleDrawableMine() != null)
                        return style.getMessageBubbleDrawableMine();

                    applyStyleMine(style);
                    if (isDefaultBubble(style, context)) {
                        topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                        bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
                            topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                        } else {
                            topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                        }
                        if (!TextUtils.isEmpty(attachment.getTitle()) && !attachment.getType().equals(ModelType.attach_file))
                            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                    }
                } else {
                    if (style.getMessageBubbleDrawableTheirs() != null)
                        return style.getMessageBubbleDrawableTheirs();

                    applyStyleTheirs(style);

                    if (isDefaultBubble(style, context)) {
                        topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                        bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
                            topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                        } else {
                            topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                        }
                        if (!TextUtils.isEmpty(attachment.getTitle()) && !attachment.getType().equals(ModelType.attach_file))
                            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);

                    }
                }
                return new DrawableBuilder()
                        .rectangle()
                        .strokeColor(strokeColor)
                        .strokeWidth(strokeWidth)
                        .solidColor(Color.WHITE)
                        .cornerRadii(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)
                        .build();
            }

            @Override
            public Drawable getDrawableForAttachmentDescription(Message message, Boolean mine, List<MessageViewHolderFactory.Position> positions){

                if (mine) {
                    applyStyleMine(style);
                    if (isDefaultBubble(style, context)) {
                        bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
                            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                        } else {
                            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                        }
                    }
                } else {
                    if (style.getMessageBubbleDrawableTheirs() != null)
                        return style.getMessageBubbleDrawableTheirs();
                    applyStyleTheirs(style);
                    if (isDefaultBubble(style, context)) {
                        bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                        if (positions.contains(MessageViewHolderFactory.Position.TOP)) {
                            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                        } else {
                            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                        }
                    }
                }

                topLeftRadius = 0;
                topRightRadius = 0;
                return new DrawableBuilder()
                        .rectangle()
                        .strokeColor(strokeColor)
                        .strokeWidth(strokeWidth)
                        .solidColor(bgColor)
                        .cornerRadii(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)
                        .build();
            }
        };
    }

    private static void applyStyleMine(MessageListViewStyle style){
        bgColor = style.getMessageBackgroundColorMine();
        strokeColor = style.getMessageBorderColorMine();
        strokeWidth = style.getMessageBorderWidthMine();
        topLeftRadius = style.getMessageTopLeftCornerRadiusMine();
        topRightRadius = style.getMessageTopRightCornerRadiusMine();
        bottomRightRadius = style.getMessageBottomRightCornerRadiusMine();
        bottomLeftRadius = style.getMessageBottomLeftCornerRadiusMine();
    }

    private static void applyStyleTheirs(MessageListViewStyle style){
        bgColor = style.getMessageBackgroundColorTheirs();
        strokeColor = style.getMessageBorderColorTheirs();
        strokeWidth = style.getMessageBorderWidthTheirs();
        topLeftRadius = style.getMessageTopLeftCornerRadiusTheirs();
        topRightRadius = style.getMessageTopRightCornerRadiusTheirs();
        bottomRightRadius = style.getMessageBottomRightCornerRadiusTheirs();
        bottomLeftRadius = style.getMessageBottomLeftCornerRadiusTheirs();
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
}
