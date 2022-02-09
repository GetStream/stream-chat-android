package io.getstream.chat.android.compose.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable

/**
 * Provides a [LazyListState] that's tied to a given message list. This is the default behavior, where we keep the base
 * scroll position of the list persisted at all times, while the thread scroll state is always new, whenever we enter a
 * thread.
 *
 * In case you want to customize the behavior, provide the [LazyListState] based on your logic and conditions.
 *
 * @param initialFirstVisibleItemIndex The first visible item index that's required for the base [LazyListState].
 * @param initialFirstVisibleItemScrollOffset The offset of the first visible item, required for the base
 * [LazyListState].
 * @param parentMessageId The ID of the parent message, if we're in a thread.
 *
 * @return [LazyListState] that keeps the scrolling position and offset of the given list.
 */
@Composable
public fun rememberMessageListState(
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0,
    parentMessageId: String? = null,
): LazyListState {
    val baseListState = rememberLazyListState(initialFirstVisibleItemIndex, initialFirstVisibleItemScrollOffset)

    return if (parentMessageId != null) {
        rememberLazyListState()
    } else {
        baseListState
    }
}
