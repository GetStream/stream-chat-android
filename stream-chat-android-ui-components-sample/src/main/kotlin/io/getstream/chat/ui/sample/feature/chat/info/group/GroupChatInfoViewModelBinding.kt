package io.getstream.chat.ui.sample.feature.chat.info.group

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoItem

fun GroupChatInfoViewModel.bindView(view: GroupChatInfoFragment, lifecycleOwner: LifecycleOwner) {

    // Update channel notifications status
    ChatClient.instance().subscribeFor<NotificationChannelMutesUpdatedEvent>(lifecycleOwner) {
        onEvent(GroupChatInfoViewModel.Event.ChannelMutesUpdated(it.me.channelMutes))
    }

    view.setMembersSeparatorClickListener {
        onEvent(GroupChatInfoViewModel.Event.MembersSeparatorClick)
    }

    view.setNameChangedListener {
        onEvent(GroupChatInfoViewModel.Event.NameChanged(it))
    }

    view.setChatInfoStatefulOptionChangedListener { option, isChecked ->
        onEvent(
            when (option) {
                is ChatInfoItem.Option.Stateful.Mute-> GroupChatInfoViewModel.Event.MuteChannelClicked(isChecked)
                else -> throw IllegalStateException("Chat info option $option is not supported!")
            }
        )
    }
    channelLeftState.observe(lifecycleOwner) {
        if (it) {
            view.navigateUpToHome()
        }
    }

    state.observe(lifecycleOwner) { state ->
        val members = if (state.shouldExpandMembers) {
            state.members.map { ChatInfoItem.MemberItem(it) }
        } else {
            state.members.take(GroupChatInfoViewModel.COLLAPSED_MEMBERS_COUNT)
                .map { ChatInfoItem.MemberItem(it) } + ChatInfoItem.MembersSeparator(state.membersToShowCount)
        }
        view.showOptions(
            members +
                listOf(
                    ChatInfoItem.Separator,
                    ChatInfoItem.ChannelName(state.channelName),
                    ChatInfoItem.Option.Stateful.Mute(isChecked = state.channelMuted),
                    ChatInfoItem.Option.SharedMedia,
                    ChatInfoItem.Option.SharedFiles,
                    ChatInfoItem.Option.LeaveGroup,
                )
        )
    }
}
