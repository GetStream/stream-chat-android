/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.state.channel.attachments

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message

/**
 * Represents the state of the channel attachments view.
 *
 * This sealed interface is used to model the different states that the channel attachments view
 * can be in, such as loading or displaying content.
 */
public sealed interface ChannelAttachmentsViewState {

    /**
     * Represents the loading state of the channel attachments view.
     */
    public data object Loading : ChannelAttachmentsViewState

    /**
     * Represents the content state of the channel attachments view.
     *
     * @param items The list of items to be displayed. Defaults to an empty list.
     * @param nextPage The next page token to be loaded *(Internal usage only)*. Defaults to null.
     * @param canLoadMore True if there are more items to be loaded. Defaults to true.
     * @param isLoadingMore True if the loading of the next page is in progress. Defaults to false.
     */
    public data class Content(
        val items: List<Item> = emptyList(),
        internal val nextPage: String? = null,
        val canLoadMore: Boolean = true,
        val isLoadingMore: Boolean = false,
    ) : ChannelAttachmentsViewState {

        /**
         * Represents a result item in the channel attachments view.
         *
         * @param message The message associated with the attachment.
         * @param attachment The attachment to be displayed.
         */
        public data class Item(
            val message: Message,
            val attachment: Attachment,
        )
    }

    /**
     * Represents the error state of the channel attachments view.
     *
     * This state is used when an error occurs while loading the items.
     *
     * @param message The error message to be displayed.
     */
    public data class Error(val message: String) : ChannelAttachmentsViewState
}
