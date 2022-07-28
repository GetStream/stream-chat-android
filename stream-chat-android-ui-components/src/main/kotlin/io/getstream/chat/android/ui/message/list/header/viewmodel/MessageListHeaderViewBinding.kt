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

@file:JvmName("MessageListHeaderViewModelBinding")

package io.getstream.chat.android.ui.message.list.header.viewmodel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
import io.getstream.chat.android.uiutils.extension.getMembersStatusText

/**
 * Binds [MessageListHeaderView] with [MessageListHeaderViewModel], updating the view's state
 * based on data provided by the ViewModel.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 */
@JvmName("bind")
public fun MessageListHeaderViewModel.bindView(view: MessageListHeaderView, lifecycle: LifecycleOwner) {
    channel.observe(lifecycle) { channel ->
        val channelName = ChatUI.channelNameFormatter.formatChannelName(
            channel = channel,
            currentUser = ChatClient.instance().getCurrentUser()
        )
        view.setTitle(channelName)
        view.setAvatar(channel)

        val memberStatus = channel.getMembersStatusText(
            context = view.context,
            currentUser = ChatUI.currentUserProvider.getCurrentUser(),
            userOnlineResId = R.string.stream_ui_user_status_online,
            userLastSeenJustNowResId = R.string.stream_ui_user_status_last_seen_just_now,
            userLastSeenResId = R.string.stream_ui_user_status_last_seen,
            memberCountResId = R.plurals.stream_ui_message_list_header_member_count,
            memberCountWithOnlineResId = R.string.stream_ui_message_list_header_member_count_online,
        )
        view.setOnlineStateSubtitle(memberStatus)
    }

    online.observe(lifecycle) { onlineState ->
        when (onlineState) {
            ConnectionState.CONNECTED -> {
                view.showOnlineStateSubtitle()
            }
            ConnectionState.CONNECTING -> {
                view.showSearchingForNetworkLabel()
            }
            ConnectionState.OFFLINE -> {
                view.showOfflineStateLabel()
            }
        }
    }

    typingUsers.observe(lifecycle, view::showTypingStateLabel)

    activeThread.observe(lifecycle) { message ->
        if (message != null) {
            view.setThreadMode()
        } else {
            view.setNormalMode()
        }
    }
}
