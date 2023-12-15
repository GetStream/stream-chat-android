package io.getstream.chat.android.ui.feature.messages.list

import io.getstream.chat.android.ui.helper.ViewStyle

/**
 * Styles container for a view that is used to display a message.
 *
 * @param own Style for messages sent by the current user.
 * @param theirs Style for messages sent by other users.
 */
public data class MessageViewStyle<T : ViewStyle>(
    val own: T,
    val theirs: T,
) : ViewStyle
