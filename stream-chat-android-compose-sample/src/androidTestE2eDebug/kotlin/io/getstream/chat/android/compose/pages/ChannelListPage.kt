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

package io.getstream.chat.android.compose.pages

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message
import io.getstream.chat.android.e2e.test.uiautomator.appContext
import io.getstream.chat.android.compose.R as ComposeR
import io.getstream.chat.android.compose.sample.R as SampleR

class ChannelListPage {

    class Header {

        companion object {
            val userAvatar = By.res("Stream_UserAvatar")
            val searchField = By.res("Stream_SearchInput")
            val createChannelButton = By.res("Stream_CreateChannelIcon")
        }
    }

    class NavigationDrawer {

        companion object {
            val reminders get() = By.text(appContext.getString(SampleR.string.navigation_drawer_later))
        }
    }

    class ChannelList {

        companion object {
            val channels = By.res("Stream_ChannelItem")
            val channelList = By.res("Stream_ChannelList")
        }

        class SwipeActions {

            companion object {
                val mute = By.desc("Mute")
                val unmute = By.desc("Unmute")
                val more = By.desc("More")
            }
        }

        class Channel {

            companion object {
                val avatar = By.res("Stream_ChannelAvatar")
                val name = By.res("Stream_ChannelName")
                val messagePreview = By.res("Stream_MessagePreview")
                val deliveryStatusIsRead = Message.deliveryStatusIsRead
                val deliveryStatusIsPending = Message.deliveryStatusIsPending
                val deliveryStatusIsSent = Message.deliveryStatusIsSent

                // The channel preview renders the failed state via MessageReadStatusIcon,
                // unlike the message list, which uses its own failed icon
                val deliveryStatusIsFailed: BySelector = By.res("Stream_MessageReadStatus_isError")
                val timestamp = By.res("Stream_Timestamp")
                val typingIndicator = By.res("Stream_ChannelListTypingIndicator")
                val mutedIcon = By.res("Stream_ChannelMutedIcon")
            }
        }
    }

    class ChannelMenu {

        companion object {
            val viewInfo = By.res("Stream_ContextMenu_View info")
            val leaveGroup = By.res("Stream_ContextMenu_Leave group")
            val deleteGroup = By.res("Stream_ContextMenu_Delete Group")

            // Leave and delete open a confirmation dialog whose confirm button is the generic
            // dialog one, not the action label.
            val confirmButton get() = By.text(appContext.getString(ComposeR.string.stream_compose_ok))
        }
    }
}
