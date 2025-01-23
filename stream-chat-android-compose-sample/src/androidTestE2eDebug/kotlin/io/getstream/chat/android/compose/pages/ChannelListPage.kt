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

package io.getstream.chat.android.compose.pages

import androidx.test.uiautomator.By
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message

class ChannelListPage {

    class Header {

        companion object {
            val userAvatar = By.res("Stream_UserAvatar")
            val searchField = By.res("Stream_SearchInput")
            val createChannelButton = "Stream_CreateChannelIcon"
        }
    }

    class ChannelList {

        companion object {
            val channels = By.res("Stream_ChannelItem")
            val channelList = By.res("Stream_ChannelList")
        }

        class Channel {

            companion object {
                val avatar = By.res("Stream_ChannelAvatar")
                val initialsAvatar = By.res("Stream_InitialsAvatar")
                val name = By.res("Stream_ChannelName")
                val messagePreview = By.res("Stream_MessagePreview")
                val deliveryStatusIsRead = Message.deliveryStatusIsRead
                val deliveryStatusIsPending = Message.deliveryStatusIsPending
                val deliveryStatusIsSent = Message.deliveryStatusIsSent
                val deliveryStatusIsFailed = Message.deliveryStatusIsFailed
                val readCount = By.res("Stream_MessageReadCount")
                val unreadCountIndicator = By.res("Stream_UnreadCountIndicator")
                val timestamp = By.res("Stream_Timestamp")
                val typingIndicator = By.res("Stream_ChannelListTypingIndicator")
                val mutedIcon = By.res("Stream_ChannelMutedIcon")
            }
        }
    }
}
