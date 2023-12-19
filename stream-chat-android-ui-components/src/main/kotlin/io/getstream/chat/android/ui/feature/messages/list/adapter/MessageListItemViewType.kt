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
}
