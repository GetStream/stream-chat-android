package io.getstream.chat.android.ui.suggestion.list.internal

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import io.getstream.chat.android.ui.common.style.TextStyle

internal data class SuggestionListViewStyle(
    @ColorInt val suggestionsBackground: Int,
    val commandsTitleTextStyle: TextStyle,
    val commandsNameTextStyle: TextStyle,
    val commandsDescriptionStyle: TextStyle,
    val mentionsUsernameTextStyle: TextStyle,
    val mentionsNameTextStyle: TextStyle,
    val mentionIcon: Drawable,
)
