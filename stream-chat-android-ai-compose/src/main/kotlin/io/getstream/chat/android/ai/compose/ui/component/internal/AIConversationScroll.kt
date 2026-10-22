/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.compose.ui.component.internal

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState

/**
 * Where the conversation scrolls when its last turn changes. A turn added after the previous last
 * one was just sent, so its top goes to the top of the list. Otherwise the conversation loaded or
 * was replaced, and it shows its end.
 */
internal sealed interface AIConversationScroll {
    /** Brings the top of the turn [turnId] to the top of the list. */
    data class ToTop(val turnId: Any) : AIConversationScroll

    /** Shows the end of the latest turn. */
    data object ToEnd : AIConversationScroll

    /** Leaves the list where it is. */
    data object None : AIConversationScroll
}

/**
 * Where to scroll now that the turns are [turnIds], oldest first, after the last one was
 * [previousLastTurnId].
 */
internal fun conversationScroll(previousLastTurnId: Any?, turnIds: List<Any>): AIConversationScroll {
    val lastTurnId = turnIds.lastOrNull()
    return when {
        lastTurnId == null || lastTurnId == previousLastTurnId -> AIConversationScroll.None
        previousLastTurnId != null && previousLastTurnId in turnIds -> AIConversationScroll.ToTop(lastTurnId)
        else -> AIConversationScroll.ToEnd
    }
}

/**
 * How far the end of the list is below what is visible, in pixels: zero at the end, and
 * [Int.MAX_VALUE] while its last item is not laid out.
 */
internal fun LazyListLayoutInfo.distanceToEnd(): Int {
    val last = visibleItemsInfo.lastOrNull() ?: return 0
    if (last.index < totalItemsCount - 1) return Int.MAX_VALUE
    return last.offset + last.size + afterContentPadding - viewportEndOffset
}

/** Shows the end of the list: the bottom of its last item and the padding after it. */
internal suspend fun LazyListState.scrollToEnd(animated: Boolean) {
    val last = layoutInfo.totalItemsCount - 1
    if (last < 0) return
    if (animated) animateScrollToItem(last) else scrollToItem(last)
    // The item's top is at the top of the list now; the rest of it may still be below.
    val distance = layoutInfo.distanceToEnd()
    if (distance in 1 until Int.MAX_VALUE) {
        if (animated) animateScrollBy(distance.toFloat()) else scrollBy(distance.toFloat())
    }
}
