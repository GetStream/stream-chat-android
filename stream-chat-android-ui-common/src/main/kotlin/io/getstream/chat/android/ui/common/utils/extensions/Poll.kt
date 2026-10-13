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

package io.getstream.chat.android.ui.common.utils.extensions

import android.content.Context
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.R
import kotlin.math.min

/**
 * Returns true if the current user can cast another vote on this poll.
 */
@InternalStreamChatApi
public fun Poll.canCastVote(): Boolean {
    val limit = maxVotesAllowed ?: return true
    return ownVotes.size < limit
}

/**
 * Returns the subtitle for the poll based on the maximum number of votes allowed.
 */
@InternalStreamChatApi
public fun Poll.getSubtitle(context: Context): String {
    if (closed) {
        return context.getString(R.string.stream_ui_poll_description_closed)
    }
    if (enforceUniqueVote) {
        return context.getString(R.string.stream_ui_poll_description_single_answer)
    }
    val maxVotes = maxVotesAllowed?.let { min(it, options.size) }
    return when (maxVotes) {
        1 -> context.getString(R.string.stream_ui_poll_description_single_answer)
        null -> context.getString(R.string.stream_ui_poll_description_unlimited_answers)
        else -> context.getString(R.string.stream_ui_poll_description_multiple_answers, maxVotes)
    }
}

/**
 * The poll name to display: the translation into [currentUser]'s language when [autoTranslationEnabled]
 * and available, the original name otherwise.
 */
@InternalStreamChatApi
public fun Poll.displayedName(currentUser: User?, autoTranslationEnabled: Boolean): String =
    translatedOrOriginal(name, currentUser, autoTranslationEnabled, ::getNameTranslation)

/**
 * The option text to display, following the same rules as [Poll.displayedName].
 */
@InternalStreamChatApi
public fun Option.displayedText(currentUser: User?, autoTranslationEnabled: Boolean): String =
    translatedOrOriginal(text, currentUser, autoTranslationEnabled, ::getTranslation)

/**
 * The answer text to display, following the same rules as [Poll.displayedName].
 */
@InternalStreamChatApi
public fun Answer.displayedText(currentUser: User?, autoTranslationEnabled: Boolean): String =
    translatedOrOriginal(text, currentUser, autoTranslationEnabled, ::getTranslation)

/**
 * Whether the poll name or any option has a translation into [currentUser]'s language. Answers are not included,
 * since each one is written by a different user.
 */
@InternalStreamChatApi
public fun Poll.hasTranslation(currentUser: User?): Boolean =
    displayedName(currentUser, autoTranslationEnabled = true) != name ||
        options.any { it.displayedText(currentUser, autoTranslationEnabled = true) != it.text }

private fun translatedOrOriginal(
    original: String,
    currentUser: User?,
    autoTranslationEnabled: Boolean,
    translation: (language: String) -> String,
): String {
    if (!autoTranslationEnabled) return original
    val language = currentUser?.language?.takeIf(String::isNotEmpty) ?: return original
    return translation(language).ifEmpty { original }
}
