package io.getstream.chat.android.ui.message.preview

import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.mention.list.MentionListView
import io.getstream.chat.android.ui.message.preview.internal.MessagePreviewView
import io.getstream.chat.android.ui.search.list.SearchResultListView

/**
 * Style for [MessagePreviewView] used by [MentionListView] and [SearchResultListView].
 *
 * @property messageSenderTextStyle - appearance for message sender text
 * @property messageTextStyle - appearance for message text
 * @property messageTimeTextStyle - appearance for message time text
 */
public data class MessagePreviewStyle(
    val messageSenderTextStyle: TextStyle,
    val messageTextStyle: TextStyle,
    val messageTimeTextStyle: TextStyle,
)
