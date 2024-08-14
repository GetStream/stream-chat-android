/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/**
 * An interface that allows to generate a title text for the given message.
 */
public fun interface SearchResultNameFormatter {

    /**
     * Generates a title text for the given message.
     *
     * @param message The message whose data is used to generate the preview text.
     * @return The formatted text representation for the given message.
     */
    public fun formatMessageTitle(message: Message, currentUser: User?): AnnotatedString

    public companion object {
        /**
         * Builds the default Search Result Name formatter.
         *
         * @return The default implementation of [SearchResultNameFormatter].
         *
         * @see [SearchResultNameFormatter]
         */
        public fun defaultFormatter(): SearchResultNameFormatter {
            return DefaultSearchResultNameFormatter
        }
    }
}

private object DefaultSearchResultNameFormatter : SearchResultNameFormatter {
    override fun formatMessageTitle(message: Message, currentUser: User?): AnnotatedString =
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(message.user.name)
            }
            message.channelInfo
                ?.takeIf { it.memberCount > 2 }
                ?.name
                ?.let {
                    append(" in ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(it)
                    }
                }
        }
}
