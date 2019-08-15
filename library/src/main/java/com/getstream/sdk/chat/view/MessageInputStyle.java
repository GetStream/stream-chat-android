
package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
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

    private boolean showAttachmentButton;

    private int attachmentButtonIcon;
    private int attachmentButtonDefaultIconColor;
    private int attachmentButtonDefaultIconPressedColor;
    private int attachmentButtonDefaultIconDisabledColor;
    private int attachmentButtonSelectedIconColor;

    private int attachmentButtonWidth;
    private int attachmentButtonHeight;

    private int inputButtonIcon;
    private int inputButtonDefaultIconColor;
    private int inputButtonDefaultIconPressedColor;
    private int inputButtonDefaultIconDisabledColor;

    private int inputButtonWidth;
    private int inputButtonHeight;

    private String inputHint;

    private int inputTextSize;
    private int inputTextColor;
    private int inputHintColor;
    private int inputTextStyle;
    private Drawable inputBackground;
    private Drawable inputSelectedBackground;
    private Drawable inputEditBackground;


    public MessageInputStyle(Context context, AttributeSet attrs) {
        setContext(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageInputView);
        // Attachment Button
        showAttachmentButton = a.getBoolean(R.styleable.MessageInputView_showAttachmentButton, true);

        attachmentButtonIcon = a.getResourceId(R.styleable.MessageInputView_attachmentButtonIcon, -1);
        attachmentButtonDefaultIconColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconColor,
                getColor(R.color.gray_dark));
        attachmentButtonDefaultIconPressedColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconPressedColor,
                getColor(R.color.white));
        attachmentButtonDefaultIconDisabledColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconDisabledColor,
                getColor(R.color.gray_light));
        attachmentButtonSelectedIconColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconDisabledColor,
                getColor(R.color.black));

        attachmentButtonWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_attachmentButtonWidth, getDimension(R.dimen.attachment_button_width));
        attachmentButtonHeight = a.getDimensionPixelSize(R.styleable.MessageInputView_attachmentButtonHeight, getDimension(R.dimen.attachment_button_height));
        // Send Button
        inputButtonIcon = a.getResourceId(R.styleable.MessageInputView_inputButtonIcon, -1);
        inputButtonDefaultIconColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultIconColor,
                getColor(R.color.input_message_send_button));
        inputButtonDefaultIconPressedColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultIconPressedColor,
                getColor(R.color.white));
        inputButtonDefaultIconDisabledColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultIconDisabledColor,
                getColor(R.color.gray_dark));

        inputButtonWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_inputButtonWidth, getDimension(R.dimen.input_button_width));
        inputButtonHeight = a.getDimensionPixelSize(R.styleable.MessageInputView_inputButtonHeight, getDimension(R.dimen.input_button_height));
        // Input Text
        inputHint = a.getString(R.styleable.MessageInputView_inputHint);

        inputTextSize = a.getDimensionPixelSize(R.styleable.MessageInputView_inputTextSize, getDimension(R.dimen.input_text_size));
        inputTextColor = a.getColor(R.styleable.MessageInputView_inputTextColor, getColor(R.color.black));
        inputHintColor = a.getColor(R.styleable.MessageInputView_inputHintColor, getColor(R.color.gray_dark));
        inputTextStyle = a.getInt(R.styleable.MessageInputView_inputTextStyle, Typeface.NORMAL);

        inputBackground = getDrawable(a.getResourceId(R.styleable.MessageInputView_inputBackground, R.drawable.round_message_composer));
        inputSelectedBackground = getDrawable(a.getResourceId(R.styleable.MessageInputView_inputSelectedBackground, R.drawable.round_message_composer_select));
        inputEditBackground = getDrawable(a.getResourceId(R.styleable.MessageInputView_inputEditBackground, R.drawable.round_message_composer_edit));

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

    // Attachment Button
    public boolean showAttachmentButton() {
        return showAttachmentButton;
    }

    public Drawable getAttachmentButtonIcon(boolean isSelected) {
        if (attachmentButtonIcon == -1) {
            return getSelector(isSelected ? attachmentButtonSelectedIconColor : attachmentButtonDefaultIconColor,
                    attachmentButtonDefaultIconPressedColor,
                    attachmentButtonDefaultIconDisabledColor,
                    R.drawable.ic_add_attachment);
        } else {
            return getDrawable(attachmentButtonIcon);
        }
    }

    public int getAttachmentButtonWidth() {
        return attachmentButtonWidth;
    }

    public int getAttachmentButtonHeight() {
        return attachmentButtonHeight;
    }

    // Send Button
    public Drawable getInputButtonIcon() {
        if (inputButtonIcon == -1) {
            return getSelector(inputButtonDefaultIconColor, inputButtonDefaultIconPressedColor,
                    inputButtonDefaultIconDisabledColor, R.drawable.ic_send);
        } else {
            return getDrawable(inputButtonIcon);
        }
    }

    public int getInputButtonWidth() {
        return inputButtonWidth;
    }

    public int getInputButtonHeight() {
        return inputButtonHeight;
    }

    // Input Text
    public String getInputHint() {
        return TextUtils.isEmpty(inputHint) ? context.getString(R.string.input_hint) : inputHint;
    }

    public int getInputTextSize() {
        return inputTextSize;
    }

    public int getInputTextColor() {
        return inputTextColor;
    }

    public int getInputHintColor() {
        return inputHintColor;
    }

    public int getInputTextStyle() {
        return inputTextStyle;
    }

    // Inputbox Background
    public Drawable getInputBackground() {
        return inputBackground;
    }

    public Drawable getInputSelectedBackground() {
        return inputSelectedBackground;
    }

    public Drawable getInputEditBackground() {
        return inputEditBackground;
    }
}
