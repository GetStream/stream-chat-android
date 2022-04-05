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

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.getLastSeenText
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.common.extensions.internal.observeTogether
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView

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

    Pair(members, membersCount).observeTogether(lifecycle) { members, membersCount ->
        view.setOnlineStateSubtitle(getOnlineStateSubtitle(view.context, members ?: emptyList(), membersCount ?: 0))
    }
}

private fun getOnlineStateSubtitle(context: Context, members: List<Member>, membersCount: Int): String {
    val users = members.map { member -> member.user }.filterCurrentUser()
    if (users.isEmpty() || membersCount == 0) return String.EMPTY

    return if (users.size == 1) {
        users.first().getLastSeenText(context)
    } else {
        getGroupSubtitle(context, members, membersCount)
    }
}

private fun List<User>.filterCurrentUser(): List<User> {
    return if (ChatClient.isInitialized) {
        val currentUser = ChatClient.instance().globalState.user.value
        filter { it.id != currentUser?.id }
    } else {
        this
    }
}

private fun getGroupSubtitle(context: Context, members: List<Member>, membersCount: Int): String {
    val allUsers = members.map { it.user }
    val onlineUsers = allUsers.count { it.online }
    val groupMembers = context.resources.getQuantityString(
        R.plurals.stream_ui_message_list_header_member_count,
        membersCount,
        membersCount
    )

    return if (onlineUsers > 0) {
        context.getString(
            R.string.stream_ui_message_list_header_member_count_online,
            groupMembers,
            onlineUsers
        )
    } else {
        groupMembers
    }
}
