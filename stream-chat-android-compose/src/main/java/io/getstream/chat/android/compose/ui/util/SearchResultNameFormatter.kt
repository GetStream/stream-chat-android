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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User

/**
 * An interface that allows to generate a title text for the given message.
 */
public fun interface SearchResultNameFormatter {

    /**
     * Generates a title text for the given search result item.
     *
     * @param searchResultItem The search result item whose data is used to generate the preview text.
     * @param currentUser The currently logged in user.
     */
    @Composable
    public fun formatMessageTitle(
        searchResultItem: ItemState.SearchResultItemState,
        currentUser: User?,
    ): AnnotatedString

    public companion object {
        /**
         * Builds the default Search Result Name formatter.
         *
         * @return The default implementation of [SearchResultNameFormatter].
         *
         * @see [SearchResultNameFormatter]
         */
        public fun defaultFormatter(): SearchResultNameFormatter = DefaultSearchResultNameFormatter
    }
}

private object DefaultSearchResultNameFormatter : SearchResultNameFormatter {
    @Composable
    override fun formatMessageTitle(
        searchResultItem: ItemState.SearchResultItemState,
        currentUser: User?,
    ): AnnotatedString = buildAnnotatedString {
        if (searchResultItem.channel?.isOneToOne(currentUser) == true) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(searchResultItem.channel.getOtherUsers(currentUser).firstOrNull()?.name)
            }
        } else {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(searchResultItem.message.user.name)
            }
            (
                searchResultItem.channel
                    ?.let { ChatTheme.channelNameFormatter.formatChannelName(it, currentUser) }
                    ?: searchResultItem.message.channelInfo?.name
                )
                ?.let { channelName ->
                    append(stringResource(R.string.stream_compose_in))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(channelName)
                    }
                }
        }
    }
}
