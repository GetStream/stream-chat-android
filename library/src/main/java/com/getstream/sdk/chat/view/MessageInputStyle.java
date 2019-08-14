
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

    private int attachmentButtonBackground;
    private int attachmentButtonDefaultBgColor;
    private int attachmentButtonDefaultBgPressedColor;
    private int attachmentButtonDefaultBgDisabledColor;

    private int attachmentButtonIcon;
    private int attachmentButtonDefaultIconColor;
    private int attachmentButtonDefaultIconPressedColor;
    private int attachmentButtonDefaultIconDisabledColor;

    private int attachmentButtonWidth;
    private int attachmentButtonHeight;
    private int attachmentButtonMargin;

    private int inputButtonBackground;
    private int inputButtonDefaultBgColor;
    private int inputButtonDefaultBgPressedColor;
    private int inputButtonDefaultBgDisabledColor;

    private int inputButtonIcon;
    private int inputButtonDefaultIconColor;
    private int inputButtonDefaultIconPressedColor;
    private int inputButtonDefaultIconDisabledColor;

    private int inputButtonWidth;
    private int inputButtonHeight;
    private int inputButtonMargin;

    private int inputMaxLines;
    private String inputHint;
    private String inputText;

    private int inputTextSize;
    private int inputTextColor;
    private int inputHintColor;

    private Drawable inputBackground;
    private Drawable inputCursorDrawable;

    private int inputDefaultPaddingLeft;
    private int inputDefaultPaddingRight;
    private int inputDefaultPaddingTop;
    private int inputDefaultPaddingBottom;

    private int delayTypingStatus;

    public MessageInputStyle (Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageInputView);

//        showAttachmentButton = a.getBoolean(R.styleable.MessageInputView_showAttachmentButton, false);
//
//        attachmentButtonBackground = a.getResourceId(R.styleable.MessageInputView_attachmentButtonBackground, -1);
//        attachmentButtonDefaultBgColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultBgColor,
//                getColor(R.color.white_four));
//        attachmentButtonDefaultBgPressedColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultBgPressedColor,
//                getColor(R.color.white_five));
//        attachmentButtonDefaultBgDisabledColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultBgDisabledColor,
//                getColor(R.color.transparent));
//
//        attachmentButtonIcon = a.getResourceId(R.styleable.MessageInputView_attachmentButtonIcon, -1);
//        attachmentButtonDefaultIconColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconColor,
//                getColor(R.color.cornflower_blue_two));
//        attachmentButtonDefaultIconPressedColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconPressedColor,
//                getColor(R.color.cornflower_blue_two_dark));
//        attachmentButtonDefaultIconDisabledColor = a.getColor(R.styleable.MessageInputView_attachmentButtonDefaultIconDisabledColor,
//                getColor(R.color.cornflower_blue_light_40));
//
//        attachmentButtonWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_attachmentButtonWidth, getDimension(R.dimen.input_button_width));
//        attachmentButtonHeight = a.getDimensionPixelSize(R.styleable.MessageInputView_attachmentButtonHeight, getDimension(R.dimen.input_button_height));
//        attachmentButtonMargin = a.getDimensionPixelSize(R.styleable.MessageInputView_attachmentButtonMargin, getDimension(R.dimen.input_button_margin));
//
//        inputButtonBackground = a.getResourceId(R.styleable.MessageInputView_inputButtonBackground, -1);
//        inputButtonDefaultBgColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultBgColor,
//                getColor(R.color.cornflower_blue_two));
//        inputButtonDefaultBgPressedColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultBgPressedColor,
//                getColor(R.color.cornflower_blue_two_dark));
//        inputButtonDefaultBgDisabledColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultBgDisabledColor,
//                getColor(R.color.white_four));
//
//        inputButtonIcon = a.getResourceId(R.styleable.MessageInputView_inputButtonIcon, -1);
//        inputButtonDefaultIconColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultIconColor,
//                getColor(R.color.white));
//        inputButtonDefaultIconPressedColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultIconPressedColor,
//                getColor(R.color.white));
//        inputButtonDefaultIconDisabledColor = a.getColor(R.styleable.MessageInputView_inputButtonDefaultIconDisabledColor,
//                getColor(R.color.warm_grey));
//
//        inputButtonWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_inputButtonWidth, getDimension(R.dimen.input_button_width));
//        inputButtonHeight = a.getDimensionPixelSize(R.styleable.MessageInputView_inputButtonHeight, getDimension(R.dimen.input_button_height));
//        inputButtonMargin = a.getDimensionPixelSize(R.styleable.MessageInputView_inputButtonMargin, getDimension(R.dimen.input_button_margin));
//
//        inputMaxLines = a.getInt(R.styleable.MessageInputView_inputMaxLines, DEFAULT_MAX_LINES);
//        inputHint = a.getString(R.styleable.MessageInputView_inputHint);
//        inputText = a.getString(R.styleable.MessageInputView_inputText);
//
//        inputTextSize = a.getDimensionPixelSize(R.styleable.MessageInputView_inputTextSize, getDimension(R.dimen.input_text_size));
//        inputTextColor = a.getColor(R.styleable.MessageInputView_inputTextColor, getColor(R.color.dark_grey_two));
//        inputHintColor = a.getColor(R.styleable.MessageInputView_inputHintColor, getColor(R.color.warm_grey_three));
//
//        inputBackground = a.getDrawable(R.styleable.MessageInputView_inputBackground);
//        inputCursorDrawable = a.getDrawable(R.styleable.MessageInputView_inputCursorDrawable);
//
//        delayTypingStatus = a.getInt(R.styleable.MessageInputView_delayTypingStatus, DEFAULT_DELAY_TYPING_STATUS);
//
//
//
//        inputDefaultPaddingLeft = getDimension(R.dimen.input_padding_left);
//        inputDefaultPaddingRight = getDimension(R.dimen.input_padding_right);
//        inputDefaultPaddingTop = getDimension(R.dimen.input_padding_top);
//        inputDefaultPaddingBottom = getDimension(R.dimen.input_padding_bottom);

        a.recycle();
    }

//    private Drawable getSelector(@ColorInt int normalColor, @ColorInt int pressedColor,
//                                 @ColorInt int disabledColor, @DrawableRes int shape) {
//
//        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
//        DrawableCompat.setTintList(
//                drawable,
//                new ColorStateList(
//                        new int[][]{
//                                new int[]{android.R.attr.state_enabled, -android.R.attr.state_pressed},
//                                new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed},
//                                new int[]{-android.R.attr.state_enabled}
//                        },
//                        new int[]{normalColor, pressedColor, disabledColor}
//                ));
//        return drawable;
//    }

    protected boolean showAttachmentButton() {
        return showAttachmentButton;
    }

//    protected Drawable getAttachmentButtonBackground() {
//        if (attachmentButtonBackground == -1) {
//            return getSelector(attachmentButtonDefaultBgColor, attachmentButtonDefaultBgPressedColor,
//                    attachmentButtonDefaultBgDisabledColor, R.drawable.mask);
//        } else {
//            return getDrawable(attachmentButtonBackground);
//        }
//    }
//
//    protected Drawable getAttachmentButtonIcon() {
//        if (attachmentButtonIcon == -1) {
//            return getSelector(attachmentButtonDefaultIconColor, attachmentButtonDefaultIconPressedColor,
//                    attachmentButtonDefaultIconDisabledColor, R.drawable.ic_add_attachment);
//        } else {
//            return getDrawable(attachmentButtonIcon);
//        }
//    }
//
//    protected int getAttachmentButtonWidth() {
//        return attachmentButtonWidth;
//    }
//
//    protected int getAttachmentButtonHeight() {
//        return attachmentButtonHeight;
//    }
//
//    protected int getAttachmentButtonMargin() {
//        return attachmentButtonMargin;
//    }
//
//    protected Drawable getInputButtonBackground() {
//        if (inputButtonBackground == -1) {
//            return getSelector(inputButtonDefaultBgColor, inputButtonDefaultBgPressedColor,
//                    inputButtonDefaultBgDisabledColor, R.drawable.mask);
//        } else {
//            return getDrawable(inputButtonBackground);
//        }
//    }
//
//    protected Drawable getInputButtonIcon() {
//        if (inputButtonIcon == -1) {
//            return getSelector(inputButtonDefaultIconColor, inputButtonDefaultIconPressedColor,
//                    inputButtonDefaultIconDisabledColor, R.drawable.ic_send);
//        } else {
//            return getDrawable(inputButtonIcon);
//        }
//    }

    protected int getInputButtonMargin() {
        return inputButtonMargin;
    }

    protected int getInputButtonWidth() {
        return inputButtonWidth;
    }

    protected int getInputButtonHeight() {
        return inputButtonHeight;
    }

    protected int getInputMaxLines() {
        return inputMaxLines;
    }

    protected String getInputHint() {
        return inputHint;
    }

    protected String getInputText() {
        return inputText;
    }

    protected int getInputTextSize() {
        return inputTextSize;
    }

    protected int getInputTextColor() {
        return inputTextColor;
    }

    protected int getInputHintColor() {
        return inputHintColor;
    }

    protected Drawable getInputBackground() {
        return inputBackground;
    }

    protected Drawable getInputCursorDrawable() {
        return inputCursorDrawable;
    }

    protected int getInputDefaultPaddingLeft() {
        return inputDefaultPaddingLeft;
    }

    protected int getInputDefaultPaddingRight() {
        return inputDefaultPaddingRight;
    }

    protected int getInputDefaultPaddingTop() {
        return inputDefaultPaddingTop;
    }

    protected int getInputDefaultPaddingBottom() {
        return inputDefaultPaddingBottom;
    }

    int getDelayTypingStatus() {
        return delayTypingStatus;
    }

}
