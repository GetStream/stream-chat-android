package com.getstream.sdk.chat.view.messageinput

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.style.TextStyle
import com.getstream.sdk.chat.view.messages.AvatarStyle

/**
 * Style for MessageInputStyle customization by xml attributes
 */
public class MessageInputStyle(private val context: Context, attrs: AttributeSet?) {
    public var inputText: TextStyle
    public var inputBackgroundText: TextStyle
    public val isShowAttachmentButton: Boolean
    public val inputButtonWidth: Int
    public val inputButtonHeight: Int
    public val attachmentButtonWidth: Int
    public val attachmentButtonHeight: Int
    public val attachmentCloseButtonBackground: Drawable?
    public val inputSendAlsoToChannelTextColor: Int
    private var inputHint = ""
    private val attachmentButtonIcon: Int
    private val attachmentButtonDefaultIconColor: Int
    private val attachmentButtonDefaultIconPressedColor: Int
    private val attachmentButtonDefaultIconDisabledColor: Int
    private val attachmentButtonSelectedIconColor: Int
    private val inputButtonIcon: Int
    private val inputButtonDefaultIconColor: Int
    private val inputButtonEditIconColor: Int
    private val inputButtonDefaultIconPressedColor: Int
    private val inputButtonDefaultIconDisabledColor: Int
    private val res = context.resources

    // Inputbox Background
    public var inputBackground: Drawable? = null
    private var inputSelectedBackground: Drawable? = null
    private var inputEditBackground: Drawable? = null

    public val avatarStyle: AvatarStyle = AvatarStyle()

    private fun getSelector(
        @ColorInt normalColor: Int,
        @ColorInt pressedColor: Int,
        @ColorInt disabledColor: Int,
        @DrawableRes shape: Int
    ): Drawable? {
        return ContextCompat.getDrawable(context, shape)?.let { drawable ->
            val drawableCompat = DrawableCompat.wrap(drawable).mutate()
            DrawableCompat.setTintList(
                drawableCompat,
                getMessageInputStyleColorList(normalColor, pressedColor, disabledColor)
            )
            drawableCompat
        }
    }

    // Attachment Button
    private val prefs: SharedPreferences // Used for write/read isShowAttachmentButton from Request permissions
    private val permissionSetKey = "permissionSetKey"

    public fun setCheckPermissions(passedPermissionCheck: Boolean) {
        prefs.edit().putBoolean(permissionSetKey, passedPermissionCheck).apply()
    }

    public fun passedPermissionCheck(): Boolean = prefs.getBoolean(permissionSetKey, false)

    private fun getMessageInputStyleColorList(normalColor: Int, pressedColor: Int, disabledColor: Int) = ColorStateList(
        arrayOf(
            intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_enabled)
        ),
        intArrayOf(normalColor, pressedColor, disabledColor)
    )

    public fun getAttachmentButtonIcon(isSelected: Boolean): Drawable? {
        return if (attachmentButtonIcon == -1) {
            getSelector(
                if (isSelected) attachmentButtonSelectedIconColor else attachmentButtonDefaultIconColor,
                attachmentButtonDefaultIconPressedColor,
                attachmentButtonDefaultIconDisabledColor,
                R.drawable.stream_ic_add_attachment
            )
        } else {
            ContextCompat.getDrawable(context, attachmentButtonIcon)
        }
    }

    // Send Button
    public fun getInputButtonIcon(isEdit: Boolean): Drawable? {
        return if (inputButtonIcon == -1) {
            getSelector(
                if (isEdit) inputButtonEditIconColor else inputButtonDefaultIconColor,
                inputButtonDefaultIconPressedColor,
                inputButtonDefaultIconDisabledColor,
                R.drawable.stream_ic_send
            )
        } else {
            ContextCompat.getDrawable(context, inputButtonIcon)
        }
    }

    // Input Text
    public fun getInputHint(): String {
        return if (TextUtils.isEmpty(inputHint)) context.getString(R.string.stream_input_hint) else inputHint
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.MessageInputView).run {
            // Attachement Button
            isShowAttachmentButton = getBoolean(R.styleable.MessageInputView_streamShowAttachmentButton, true)
            attachmentButtonIcon = getResourceId(R.styleable.MessageInputView_streamAttachmentButtonIcon, -1)
            attachmentButtonDefaultIconColor = getColor(
                R.styleable.MessageInputView_streamAttachmentButtonDefaultIconColor,
                ContextCompat.getColor(context, R.color.stream_gray_dark)
            )
            attachmentButtonDefaultIconPressedColor = getColor(
                R.styleable.MessageInputView_streamAttachmentButtonDefaultIconPressedColor,
                ContextCompat.getColor(context, R.color.stream_white)
            )
            attachmentButtonDefaultIconDisabledColor = getColor(
                R.styleable.MessageInputView_streamAttachmentButtonDefaultIconDisabledColor,
                ContextCompat.getColor(context, R.color.stream_gray_light)
            )
            attachmentButtonSelectedIconColor = getColor(
                R.styleable.MessageInputView_streamAttachmentButtonSelectedIconColor,
                ContextCompat.getColor(context, R.color.stream_black)
            )
            attachmentButtonWidth = getDimensionPixelSize(
                R.styleable.MessageInputView_streamAttachmentButtonWidth,
                res.getDimensionPixelSize(R.dimen.stream_attachment_button_width)
            )
            attachmentButtonHeight = getDimensionPixelSize(
                R.styleable.MessageInputView_streamAttachmentButtonHeight,
                res.getDimensionPixelSize(R.dimen.stream_attachment_button_height)
            )
            attachmentCloseButtonBackground =
                getDrawable(R.styleable.MessageInputView_streamAttachmentCloseButtonBackground)
                    ?: ContextCompat.getDrawable(context, R.drawable.stream_button_close)

            // Send Button
            inputButtonIcon = getResourceId(R.styleable.MessageInputView_streamInputButtonIcon, -1)
            inputButtonDefaultIconColor = getColor(
                R.styleable.MessageInputView_streamInputButtonDefaultIconColor,
                ContextCompat.getColor(context, R.color.stream_input_message_send_button)
            )
            inputButtonEditIconColor = getColor(
                R.styleable.MessageInputView_streamInputButtonEditIconColor,
                ContextCompat.getColor(context, R.color.stream_input_message_box_stroke_edit)
            )
            inputButtonDefaultIconPressedColor = getColor(
                R.styleable.MessageInputView_streamInputButtonDefaultIconPressedColor,
                ContextCompat.getColor(context, R.color.stream_white)
            )
            inputButtonDefaultIconDisabledColor = getColor(
                R.styleable.MessageInputView_streamInputButtonDefaultIconDisabledColor,
                ContextCompat.getColor(context, R.color.stream_gray_dark)
            )
            inputButtonWidth = getDimensionPixelSize(
                R.styleable.MessageInputView_streamInputButtonWidth,
                res.getDimensionPixelSize(R.dimen.stream_input_button_width)
            )
            inputButtonHeight = getDimensionPixelSize(
                R.styleable.MessageInputView_streamInputButtonHeight,
                res.getDimensionPixelSize(R.dimen.stream_input_button_height)
            )

            // Input Text
            getString(R.styleable.MessageInputView_streamInputHint)?.let { inputHint = it }
            inputText = TextStyle.Builder(this).apply {
                size(
                    R.styleable.MessageInputView_streamInputTextSize,
                    res.getDimensionPixelSize(R.dimen.stream_input_text_size)
                )
                color(
                    R.styleable.MessageInputView_streamInputTextColor,
                    ContextCompat.getColor(context, R.color.stream_black)
                )
                hintColor(
                    R.styleable.MessageInputView_streamInputHintColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                )
                font(
                    R.styleable.MessageInputView_streamInputTextFontAssets,
                    R.styleable.MessageInputView_streamInputTextFont
                )
                style(R.styleable.MessageInputView_streamInputTextStyle, Typeface.NORMAL)
            }.build()

            context.getDrawable(getResourceId(R.styleable.MessageInputView_streamInputBackground, R.drawable.stream_round_message_composer))?.let { inputBackground = it }
            context.getDrawable(getResourceId(R.styleable.MessageInputView_streamInputSelectedBackground, R.drawable.stream_round_message_composer_select))?.let { inputSelectedBackground = it }
            context.getDrawable(
                getResourceId(
                    R.styleable.MessageInputView_streamInputEditBackground,
                    R.drawable.stream_round_message_composer_edit
                )
            )?.let { inputEditBackground = it }

            inputBackgroundText = TextStyle.Builder(this).apply {
                size(
                    R.styleable.MessageInputView_streamInputBackgroundTextSize,
                    res.getDimensionPixelSize(R.dimen.stream_input_text_size)
                )
                color(
                    R.styleable.MessageInputView_streamInputBackgroundTextColor,
                    ContextCompat.getColor(context, R.color.stream_black)
                )
                font(
                    R.styleable.MessageInputView_streamInputBackgroundTextFontAssets,
                    R.styleable.MessageInputView_streamInputBackgroundTextFont
                )
                style(R.styleable.MessageInputView_streamInputBackgroundTextStyle, Typeface.NORMAL)
            }.build()

            // Avatar
            avatarStyle.avatarWidth = getDimensionPixelSize(
                R.styleable.MessageInputView_streamAvatarWidth,
                res.getDimensionPixelSize(R.dimen.stream_message_avatar_width)
            )
            avatarStyle.avatarHeight = getDimensionPixelSize(
                R.styleable.MessageInputView_streamAvatarHeight,
                res.getDimensionPixelSize(R.dimen.stream_message_avatar_height)
            )
            avatarStyle.avatarBackGroundColor = getColor(
                R.styleable.MessageInputView_streamAvatarBackGroundColor,
                ContextCompat.getColor(context, R.color.stream_gray_dark)
            )
            avatarStyle.avatarInitialText = TextStyle.Builder(this).apply {
                size(
                    R.styleable.MessageInputView_streamAvatarTextSize,
                    res.getDimensionPixelSize(R.dimen.stream_channel_initials)
                )
                color(R.styleable.MessageInputView_streamAvatarTextColor, Color.WHITE)
                font(
                    R.styleable.MessageInputView_streamAvatarTextFontAssets,
                    R.styleable.MessageInputView_streamAvatarTextFont
                )
                style(R.styleable.MessageInputView_streamAvatarTextStyle, Typeface.BOLD)
            }.build()

            inputSendAlsoToChannelTextColor = getColor(
                R.styleable.MessageInputView_streamSendAlsoToChannel,
                ContextCompat.getColor(
                    context,
                    R.color.stream_input_message_send_to_channel_checkbox
                )
            )

            prefs = context.getSharedPreferences(
                "MessageInputStyle",
                Context.MODE_PRIVATE
            )
            recycle()
        }
    }
}
