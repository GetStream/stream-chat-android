/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.adapter

/**
 * View type constants used by [MessageListItemViewHolderFactory].
 */
public object MessageListItemViewType {
    // A base offset to avoid clashes between built-in and custom view types
    private const val OFFSET = 1000

    public const val DATE_DIVIDER: Int = OFFSET + 1
    public const val MESSAGE_DELETED: Int = OFFSET + 2
    public const val PLAIN_TEXT: Int = OFFSET + 3
    public const val CUSTOM_ATTACHMENTS: Int = OFFSET + 4
    public const val LOADING_INDICATOR: Int = OFFSET + 5
    public const val THREAD_SEPARATOR: Int = OFFSET + 6
    public const val TYPING_INDICATOR: Int = OFFSET + 7
    public const val GIPHY: Int = OFFSET + 8
    public const val SYSTEM_MESSAGE: Int = OFFSET + 9
    public const val ERROR_MESSAGE: Int = OFFSET + 10
    public const val THREAD_PLACEHOLDER: Int = OFFSET + 11
    public const val GIPHY_ATTACHMENT: Int = OFFSET + 12
    public const val MEDIA_ATTACHMENT: Int = OFFSET + 13
    public const val FILE_ATTACHMENTS: Int = OFFSET + 14
    public const val LINK_ATTACHMENTS: Int = OFFSET + 15
    public const val UNREAD_SEPARATOR: Int = OFFSET + 16
    public const val START_OF_THE_CHANNEL: Int = OFFSET + 17
    public const val POLL: Int = OFFSET + 18

    public fun toString(viewType: Int): String = when (viewType) {
        DATE_DIVIDER -> "DATE_DIVIDER"
        MESSAGE_DELETED -> "MESSAGE_DELETED"
        PLAIN_TEXT -> "PLAIN_TEXT"
        CUSTOM_ATTACHMENTS -> "CUSTOM_ATTACHMENTS"
        LOADING_INDICATOR -> "LOADING_INDICATOR"
        THREAD_SEPARATOR -> "THREAD_SEPARATOR"
        TYPING_INDICATOR -> "TYPING_INDICATOR"
        GIPHY -> "GIPHY"
        SYSTEM_MESSAGE -> "SYSTEM_MESSAGE"
        ERROR_MESSAGE -> "ERROR_MESSAGE"
        THREAD_PLACEHOLDER -> "THREAD_PLACEHOLDER"
        GIPHY_ATTACHMENT -> "GIPHY_ATTACHMENT"
        MEDIA_ATTACHMENT -> "MEDIA_ATTACHMENT"
        FILE_ATTACHMENTS -> "FILE_ATTACHMENTS"
        LINK_ATTACHMENTS -> "LINK_ATTACHMENTS"
        UNREAD_SEPARATOR -> "UNREAD_SEPARATOR"
        START_OF_THE_CHANNEL -> "START_OF_THE_CHANNEL"
        POLL -> "POLL"
        else -> "UNKNOWN"
    }
}
