package io.getstream.chat.android.ui.suggestion.list

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 ** Style for [SuggestionListView]
 *
 * Use this class to style SuggestionListView programmatically. You can pass this class to [TransformStyle.suggestionListStyleTransformer]
 * to change the configuration of the View.
 *
 * @constructor Create the data class with all the information necessary to customize SuggestionListView
 *
 * @property suggestionsBackground Background color of the suggestions box (command and mentions).
 * @property commandsTitleTextStyle Sets the styles for title for the commands box. Use to customise size, colour, font and style (ex: bold).
 * @property commandsNameTextStyle Sets the styles for the name of the command in the command list. Use to customise size, colour, font and style (ex: bold)
 * @property commandsDescriptionTextStyle Sets the styles for the description of the command in the command list. Use to customise size, colour, font and style (ex: bold).
 * @property mentionsUsernameTextStyle Configure the appearance for username in the mention list.
 * @property mentionsNameTextStyle Configure the appearance for username in the mention list.
 * @property mentionIcon Icon for mentions. It is normally "@".
 * @property commandIcon Icon for each command item. Default value is [R.drawable.stream_ui_ic_command_circle].
 * @property lightningIcon Icon to the left of command list title. Default value is [R.drawable.stream_ui_ic_command_blue].
 */
public data class SuggestionListViewStyle(
    @ColorInt val suggestionsBackground: Int,
    val commandsTitleTextStyle: TextStyle,
    val commandsNameTextStyle: TextStyle,
    val commandsDescriptionTextStyle: TextStyle,
    val mentionsUsernameTextStyle: TextStyle,
    val mentionsNameTextStyle: TextStyle,
    val mentionIcon: Drawable,
    val commandIcon: Drawable,
    val lightningIcon: Drawable,
) {
    internal companion object {
        operator fun invoke(context: Context, attrs: AttributeSet?): SuggestionListViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.MessageInputView,
                R.attr.streamUiSuggestionListViewStyle,
                R.style.StreamUi_SuggestionListView,
            ).use { a ->

                val suggestionsBackground = a.getColor(
                    R.styleable.MessageInputView_streamUiSuggestionBackgroundColor,
                    context.getColorCompat(R.color.stream_ui_white)
                )

                val commandsTitleTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCommandsTitleTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsTitleTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiCommandsTitleFontAssets,
                        R.styleable.MessageInputView_streamUiCommandsTitleFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiCommandsTitleStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val commandsNameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCommandsNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiCommandsNameFontAssets,
                        R.styleable.MessageInputView_streamUiCommandsNameFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiCommandsNameStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val commandsDescriptionTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionFontAssets,
                        R.styleable.MessageInputView_streamUiCommandsDescriptionFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiCommandsDescriptionStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val mentionsUsernameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiMentionsUserNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMentionsUserNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_primary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiMentionsUserNameFontAssets,
                        R.styleable.MessageInputView_streamUiMentionsUserNameFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiMentionsUserNameStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val mentionsNameTextStyle = TextStyle.Builder(a)
                    .size(
                        R.styleable.MessageInputView_streamUiMentionsNameTextSize,
                        context.getDimension(R.dimen.stream_ui_text_medium)
                    )
                    .color(
                        R.styleable.MessageInputView_streamUiMentionsNameTextColor,
                        context.getColorCompat(R.color.stream_ui_text_color_secondary)
                    )
                    .font(
                        R.styleable.MessageInputView_streamUiMentionsNameFontAssets,
                        R.styleable.MessageInputView_streamUiMentionsNameFont
                    )
                    .style(
                        R.styleable.MessageInputView_streamUiMentionsNameStyle,
                        Typeface.NORMAL
                    )
                    .build()

                val mentionIcon: Drawable = a.getDrawable(R.styleable.MessageInputView_streamUiMentionsIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_mention)!!

                val commandIcon: Drawable = a.getDrawable(R.styleable.MessageInputView_streamUiCommandIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command_circle)!!

                val lightningIcon: Drawable = a.getDrawable(R.styleable.MessageInputView_streamUiLightningIcon)
                    ?: context.getDrawableCompat(R.drawable.stream_ui_ic_command_blue)!!

                return SuggestionListViewStyle(
                    suggestionsBackground = suggestionsBackground,
                    commandsTitleTextStyle = commandsTitleTextStyle,
                    commandsNameTextStyle = commandsNameTextStyle,
                    commandsDescriptionTextStyle = commandsDescriptionTextStyle,
                    mentionsUsernameTextStyle = mentionsUsernameTextStyle,
                    mentionsNameTextStyle = mentionsNameTextStyle,
                    mentionIcon = mentionIcon,
                    commandIcon = commandIcon,
                    lightningIcon = lightningIcon,
                ).let(TransformStyle.suggestionListStyleTransformer::transform)
            }
        }

        fun createDefault(context: Context) = invoke(context, null)
    }
}
