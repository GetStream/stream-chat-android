@file:JvmName("MessageListHeaderViewModelBinding")

package io.getstream.chat.android.ui.message.list.header.viewmodel

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.getDisplayName
import io.getstream.chat.android.ui.common.extensions.getLastSeenText
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
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
    channelState.observe(lifecycle) {
        view.setTitle(it.getDisplayName(view.context, R.string.stream_ui_message_list_header_untitled_channel))
        view.setAvatar(it)
    }

    online.observe(lifecycle) { isOnline ->
        if (isOnline) {
            view.showOnlineStateSubtitle()
        } else {
            view.showSearchingForNetworkLabel()
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

    members.observe(lifecycle) { memberList ->
        view.setOnlineStateSubtitle(getOnlineStateSubtitle(view.context, memberList))
    }
}

private fun getOnlineStateSubtitle(context: Context, members: List<Member>): String {
    val users = members.map { member -> member.user }.filterCurrentUser()
    if (users.isEmpty()) return String.EMPTY

    return if (users.size == 1) {
        users.first().getLastSeenText(context)
    } else {
        getGroupSubtitle(context, members)
    }
}

private fun List<User>.filterCurrentUser(): List<User> {
    return if (ChatDomain.isInitialized) {
        val currentUser = ChatDomain.instance().user.value
        filter { it.id != currentUser?.id }
    } else {
        this
    }
}

private fun getGroupSubtitle(context: Context, members: List<Member>): String {
    val allUsers = members.map { it.user }
    val onlineUsers = allUsers.count { it.online }
    val groupMembers = context.resources.getQuantityString(
        R.plurals.stream_ui_message_list_header_member_count,
        allUsers.size,
        allUsers.size
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
