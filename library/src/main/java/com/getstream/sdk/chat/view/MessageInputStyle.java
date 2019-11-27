package com.getstream.sdk.chat.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.graphics.drawable.DrawableCompat;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.style.TextStyle;

/**
 * Style for MessageInputStyle customization by xml attributes
 */
public class MessageInputStyle extends BaseStyle {

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
    private int inputButtonEditIconColor;
    private int inputButtonDefaultIconPressedColor;
    private int inputButtonDefaultIconDisabledColor;

    private int inputButtonWidth;
    private int inputButtonHeight;

    private String inputHint;

    public TextStyle inputText;

    private Drawable inputBackground;
    private Drawable inputSelectedBackground;
    private Drawable inputEditBackground;

    public TextStyle inputBackgroundText;

    public MessageInputStyle(Context context, AttributeSet attrs) {
        setContext(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MessageInputView);
        // Attachment Button
        showAttachmentButton = a.getBoolean(R.styleable.MessageInputView_streamShowAttachmentButton, true);

        attachmentButtonIcon = a.getResourceId(R.styleable.MessageInputView_streamAttachmentButtonIcon, -1);
        attachmentButtonDefaultIconColor = a.getColor(R.styleable.MessageInputView_streamAttachmentButtonDefaultIconColor,
                getColor(R.color.stream_gray_dark));
        attachmentButtonDefaultIconPressedColor = a.getColor(R.styleable.MessageInputView_streamAttachmentButtonDefaultIconPressedColor,
                getColor(R.color.stream_white));
        attachmentButtonDefaultIconDisabledColor = a.getColor(R.styleable.MessageInputView_streamAttachmentButtonDefaultIconDisabledColor,
                getColor(R.color.stream_gray_light));
        attachmentButtonSelectedIconColor = a.getColor(R.styleable.MessageInputView_streamAttachmentButtonDefaultIconDisabledColor,
                getColor(R.color.stream_black));

        attachmentButtonWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_streamAttachmentButtonWidth, getDimension(R.dimen.stream_attachment_button_width));
        attachmentButtonHeight = a.getDimensionPixelSize(R.styleable.MessageInputView_streamAttachmentButtonHeight, getDimension(R.dimen.stream_attachment_button_height));
        // Send Button
        inputButtonIcon = a.getResourceId(R.styleable.MessageInputView_streamInputButtonIcon, -1);
        inputButtonDefaultIconColor = a.getColor(R.styleable.MessageInputView_streamInputButtonDefaultIconColor,
                getColor(R.color.stream_input_message_send_button));
        inputButtonEditIconColor = a.getColor(R.styleable.MessageInputView_streamInputButtonEditIconColor,
                getColor(R.color.stream_input_message_box_stroke_edit));
        inputButtonDefaultIconPressedColor = a.getColor(R.styleable.MessageInputView_streamInputButtonDefaultIconPressedColor,
                getColor(R.color.stream_white));
        inputButtonDefaultIconDisabledColor = a.getColor(R.styleable.MessageInputView_streamInputButtonDefaultIconDisabledColor,
                getColor(R.color.stream_gray_dark));

        inputButtonWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_streamInputButtonWidth, getDimension(R.dimen.stream_input_button_width));
        inputButtonHeight = a.getDimensionPixelSize(R.styleable.MessageInputView_streamInputButtonHeight, getDimension(R.dimen.stream_input_button_height));
        // Input Text
        inputHint = a.getString(R.styleable.MessageInputView_streamInputHint);

        inputText = new TextStyle.Builder(a)
                .size(R.styleable.MessageInputView_streamInputTextSize, getDimension(R.dimen.stream_input_text_size))
                .color(R.styleable.MessageInputView_streamInputTextColor, getColor(R.color.stream_black))
                .hintColor(R.styleable.MessageInputView_streamInputHintColor, getColor(R.color.stream_gray_dark))
                .font(R.styleable.MessageInputView_streamInputTextFontAssets, R.styleable.MessageInputView_streamInputTextFont)
                .style(R.styleable.MessageInputView_streamInputTextStyle, Typeface.NORMAL)
                .build();

        inputBackground = getDrawable(a.getResourceId(R.styleable.MessageInputView_streamInputBackground, R.drawable.stream_round_message_composer));
        inputSelectedBackground = getDrawable(a.getResourceId(R.styleable.MessageInputView_streamInputSelectedBackground, R.drawable.stream_round_message_composer_select));
        inputEditBackground = getDrawable(a.getResourceId(R.styleable.MessageInputView_streamInputEditBackground, R.drawable.stream_round_message_composer_edit));

        inputBackgroundText = new TextStyle.Builder(a)
                .size(R.styleable.MessageInputView_streamInputBackgroundTextSize, getDimension(R.dimen.stream_input_text_size))
                .color(R.styleable.MessageInputView_streamInputBackgroundTextColor, getColor(R.color.stream_black))
                .font(R.styleable.MessageInputView_streamInputBackgroundTextFontAssets, R.styleable.MessageInputView_streamInputBackgroundTextFont)
                .style(R.styleable.MessageInputView_streamInputBackgroundTextStyle, Typeface.NORMAL)
                .build();

        // Avatar
        avatarWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_streamAvatarWidth, getDimension(R.dimen.stream_message_avatar_width));
        avatarHeight = a.getDimensionPixelSize(R.styleable.MessageInputView_streamAvatarHeight, getDimension(R.dimen.stream_message_avatar_height));

        avatarBorderWidth = a.getDimensionPixelSize(R.styleable.MessageInputView_streamAvatarBorderWidth, getDimension(R.dimen.stream_channel_avatar_border_width));
        avatarBorderColor = a.getColor(R.styleable.MessageInputView_streamAvatarBorderColor, Color.WHITE);
        avatarBackGroundColor = a.getColor(R.styleable.MessageInputView_streamAvatarBackGroundColor, getColor(R.color.stream_gray_dark));

        avatarInitialText = new TextStyle.Builder(a)
                .size(R.styleable.MessageInputView_streamAvatarTextSize, getDimension(R.dimen.stream_channel_initials))
                .color(R.styleable.MessageInputView_streamAvatarTextColor, Color.WHITE)
                .font(R.styleable.MessageInputView_streamAvatarTextFontAssets, R.styleable.MessageInputView_streamAvatarTextFont)
                .style(R.styleable.MessageInputView_streamAvatarTextStyle, Typeface.BOLD)
                .build();

        prefs = context.getSharedPreferences(
                "MessageInputStyle", Context.MODE_PRIVATE);
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
    private SharedPreferences prefs; // Used for write/read isShowAttachmentButton from Request permissions

    private final String permissionSetKey = "permissionSetKey";
    public boolean isShowAttachmentButton() {
        return showAttachmentButton;
    }

    public void setCheckPermissions(boolean passedPermissionCheck) {
        prefs.edit().putBoolean(permissionSetKey, passedPermissionCheck).apply();
    }

    public boolean passedPermissionCheck() {
        return prefs.getBoolean(permissionSetKey, false);
    }

    public Drawable getAttachmentButtonIcon(boolean isSelected) {
        if (attachmentButtonIcon == -1) {
            return getSelector(isSelected ? attachmentButtonSelectedIconColor : attachmentButtonDefaultIconColor,
                    attachmentButtonDefaultIconPressedColor,
                    attachmentButtonDefaultIconDisabledColor,
                    R.drawable.stream_ic_add_attachment);
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
    public Drawable getInputButtonIcon(boolean isEdit) {
        if (inputButtonIcon == -1) {
            return getSelector(isEdit ? inputButtonEditIconColor : inputButtonDefaultIconColor , inputButtonDefaultIconPressedColor,
                    inputButtonDefaultIconDisabledColor, R.drawable.stream_ic_send);
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
        return TextUtils.isEmpty(inputHint) ? context.getString(R.string.stream_input_hint) : inputHint;
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
