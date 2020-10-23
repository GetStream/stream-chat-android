package com.getstream.sdk.chat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Message;
import top.defaults.drawabletoolbox.DrawableBuilder;

public class DefaultBubbleHelper {
    private static int topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius;
    private static int bgColor, strokeColor, strokeWidth;

    public static MessageListView.BubbleHelper initDefaultBubbleHelper(MessageListViewStyle style, Context context) {
        return new MessageListView.BubbleHelper() {
            @NonNull
            @Override
            public Drawable getDrawableForMessage(@NotNull Message message, boolean mine, @NotNull List<? extends MessageListItem.Position> positions) {
                if (style.getMessageBubbleDrawable(mine) != -1)
                    return ContextCompat.getDrawable(context, style.getMessageBubbleDrawable(mine));

                configParams(style, mine, false);
                if (isDefaultBubble(style, mine, context))
                    applyStyleDefault(positions, mine, context);
                if (mine) {
                    // set background for Failed or Error message
                    //if (message.getSyncStatus() == Sync.LOCAL_FAILED
                    //TODO: llc: check cache
                    if (message.getType().equals(ModelType.message_error))
                        bgColor = context.getResources().getColor(R.color.stream_message_failed);
                }
                return getBubbleDrawable();
            }

            @NotNull
            @Override
            public Drawable getDrawableForAttachment(@NotNull Message message, boolean mine, @NotNull List<? extends MessageListItem.Position> positions, @NotNull Attachment attachment) {
                if (attachment.getType() == null || attachment.getType().equals(ModelType.attach_unknown))
                    return null;

                if (style.getMessageBubbleDrawable(mine) != -1)
                    return ContextCompat.getDrawable(context, style.getMessageBubbleDrawable(mine));

                configParams(style, mine, true);
                if (isDefaultBubble(style, mine, context))
                    applyStyleDefault(positions, mine, context);

                // set corner radius if the attachment has title or description
                if (!TextUtils.isEmpty(attachment.getTitle()) && !attachment.getType().equals(ModelType.attach_file))
                    bottomLeftRadius = bottomRightRadius = 0;
                // set corner radius if the attachment is not first
                if (message.getAttachments().indexOf(attachment) != 0) {
                    if (mine)
                        topRightRadius = 0;
                    else
                        topLeftRadius = 0;
                }
                return getBubbleDrawable();
            }

            @NotNull
            @Override
            public Drawable getDrawableForAttachmentDescription(@NotNull Message message, boolean mine, @NotNull List<? extends MessageListItem.Position> positions) {
                if (style.getMessageBubbleDrawable(mine) != -1)
                    return ContextCompat.getDrawable(context, style.getMessageBubbleDrawable(mine));

                configParams(style, mine, true);
                if (isDefaultBubble(style, mine, context))
                    applyStyleDefault(positions, mine, context);

                topLeftRadius = topRightRadius = 0;
                return getBubbleDrawable();
            }
        };
    }

    private static void configParams(MessageListViewStyle style, boolean isMine, boolean isAttachment) {
        bgColor = isAttachment ? style.getAttachmentBackgroundColor(isMine) : style.getMessageBackgroundColor(isMine);
        strokeColor = isAttachment ? style.getAttachmentBorderColor(isMine) : style.getMessageBorderColor(isMine);
        strokeWidth = style.getMessageBorderWidth(isMine);
        topLeftRadius = style.getMessageTopLeftCornerRadius(isMine);
        topRightRadius = style.getMessageTopRightCornerRadius(isMine);
        bottomRightRadius = style.getMessageBottomRightCornerRadius(isMine);
        bottomLeftRadius = style.getMessageBottomLeftCornerRadius(isMine);
    }

    private static void applyStyleDefault(List<? extends MessageListItem.Position> positions, boolean isMine, Context context) {
        if (isMine) {
            topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            if (positions.contains(MessageListItem.Position.TOP)) {
                topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            } else {
                topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            }
        } else {
            topRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            bottomRightRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
            if (positions.contains(MessageListItem.Position.TOP)) {
                topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1);
                bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            } else {
                topLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
                bottomLeftRadius = context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2);
            }
        }
    }

    private static boolean isDefaultBubble(MessageListViewStyle style, boolean isMine, Context context) {
        if (isMine)
            return (style.getMessageTopLeftCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                    (style.getMessageTopRightCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                    (style.getMessageBottomRightCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2)) &&
                    (style.getMessageBottomLeftCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1));

        return (style.getMessageTopLeftCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageTopRightCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageBottomRightCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius1)) &&
                (style.getMessageBottomLeftCornerRadius(isMine) == context.getResources().getDimensionPixelSize(R.dimen.stream_message_corner_radius2));
    }

    private static Drawable getBubbleDrawable() {
        return new DrawableBuilder()
                .rectangle()
                .strokeColor(strokeColor)
                .strokeWidth(strokeWidth)
                .solidColor(bgColor)
                .cornerRadii(topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)
                .build();
    }
}