package io.getstream.chat.android.ui.common.state.pinned

import io.getstream.chat.android.models.Message
import java.util.Date

/**
 * Represents the pinned message list state, used to render the required UI.
 *
 * @param canLoadMore Indicator if we've reached the end of messages, to stop triggering pagination.
 * @param results The messages to render.
 * @param isLoading Indicator if we're currently loading data (initial load).
 * @param nextDate Date used to fetch next page of the messages.
 */
public data class PinnedMessageListState(
    val canLoadMore: Boolean,
    val results: List<Message>,
    val isLoading: Boolean,
    val nextDate: Date
)
