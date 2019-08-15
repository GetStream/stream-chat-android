
package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.BaseStyle;

/**
 * Style for MessageInputStyle customization by xml attributes
 */
class MessageInputStyle extends BaseStyle {

    private static final int DEFAULT_MAX_LINES = 5;
    private static final int DEFAULT_DELAY_TYPING_STATUS = 1500;

    private boolean showAttachmentButton;
    private int attachmentButtonActiveDrawble;
    private int attachmentButtonDisabledDrawble;

    private int sendButtonActiveDrawble;
    private int sendButtonDisabledDrawble;

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
        setContext(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageInputView);

        showAttachmentButton = a.getBoolean(R.styleable.MessageInputView_showAttachmentButton, false);



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
