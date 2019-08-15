
package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.graphics.drawable.DrawableCompat;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

/**
 * Style for MessageInputStyle customization by xml attributes
 */
class MessageInputStyle extends BaseStyle {

    private static final int DEFAULT_MAX_LINES = 5;
    private static final int DEFAULT_DELAY_TYPING_STATUS = 1500;

    private boolean showAttachmentButton;

    private int attachmentButtonIcon;
    private int attachmentButtonDefaultIconColor;
    private int attachmentButtonDefaultIconPressedColor;
    private int attachmentButtonDefaultIconDisabledColor;

    private int attachmentButtonWidth;
    private int attachmentButtonHeight;
    private int attachmentButtonMargin;


    public MessageInputStyle (Context context, AttributeSet attrs) {
        setContext(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageInputView);
        showAttachmentButton = a.getBoolean(R.styleable.MessageInputView_showAttachmentButton, true);

        attachmentButtonIcon = a.getResourceId(R.styleable.MessageInputView_attachmentButtonIcon, -1);
        attachmentButtonDefaultIconColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconColor,
                getColor(R.color.gray_dark));
        attachmentButtonDefaultIconPressedColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconPressedColor,
                getColor(R.color.black));
        attachmentButtonDefaultIconDisabledColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconDisabledColor,
                getColor(R.color.gray_light));

        attachmentButtonWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_attachmentButtonWidth, getDimension(R.dimen.attachment_button_width));
        attachmentButtonHeight = a.getDimensionPixelSize(R.styleable.MessageInputView_attachmentButtonHeight, getDimension(R.dimen.attachment_button_height));
        attachmentButtonMargin = a.getDimensionPixelSize(R.styleable.MessageInputView_attachmentButtonMargin, getDimension(R.dimen.input_button_margin));

        a.recycle();
    }

    private Drawable getSelector(@ColorInt int normalColor, @ColorInt int pressedColor,
                                 @ColorInt int disabledColor, @DrawableRes int shape) {

        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_enabled, -android.R.attr.state_pressed},
                                new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_enabled}
                        },
                        new int[]{normalColor, pressedColor, disabledColor}
                ));
        return drawable;
    }

    protected boolean showAttachmentButton() {
        return showAttachmentButton;
    }

    protected Drawable getAttachmentButtonIcon() {
        if (attachmentButtonIcon == -1) {
            return getSelector(attachmentButtonDefaultIconColor, attachmentButtonDefaultIconPressedColor,
                    attachmentButtonDefaultIconDisabledColor, R.drawable.ic_add_attachment);
        } else {
            return getDrawable(attachmentButtonIcon);
        }
    }

    protected int getAttachmentButtonWidth() {
        return attachmentButtonWidth;
    }

    protected int getAttachmentButtonHeight() {
        return attachmentButtonHeight;
    }

    protected int getAttachmentButtonMargin() {
        return attachmentButtonMargin;
    }

}
