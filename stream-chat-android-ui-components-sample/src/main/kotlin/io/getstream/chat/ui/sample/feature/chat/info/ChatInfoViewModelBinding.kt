package io.getstream.chat.ui.sample.feature.chat.info

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.subscribeFor

fun ChatInfoViewModel.bindView(view: ChatInfoFragment, lifecycleOwner: LifecycleOwner) {
    view.setChatInfoStatefulOptionChangedListener { option, isChecked ->
        onEvent(
            when (option) {
                is ChatInfoItem.Option.Stateful.Notifications -> ChatInfoViewModel.Event.OptionNotificationClicked(
                    isChecked
                )
                is ChatInfoItem.Option.Stateful.MuteUser -> ChatInfoViewModel.Event.OptionMuteUserClicked(isChecked)
                is ChatInfoItem.Option.Stateful.Block -> ChatInfoViewModel.Event.OptionBlockUserClicked(isChecked)
                else -> throw IllegalStateException("Chat info option $option is not supported!")
            }
        )
    }

    // Update channel notifications status
    ChatClient.instance().subscribeFor<NotificationChannelMutesUpdatedEvent>(lifecycleOwner) {
        onEvent(ChatInfoViewModel.Event.ChannelMutesUpdated(it.me.channelMutes))
    }

    state.observe(lifecycleOwner) { state ->
        view.showOptions(
            listOf(
                ChatInfoItem.MemberItem(state.member),
                ChatInfoItem.Separator,
                ChatInfoItem.Option.Stateful.Notifications(isChecked = state.notificationsEnabled),
                ChatInfoItem.Option.Stateful.MuteUser(isChecked = state.isMemberMuted),
                ChatInfoItem.Option.Stateful.Block(isChecked = state.isMemberBlocked),
                ChatInfoItem.Option.SharedMedia,
                ChatInfoItem.Option.SharedFiles,
                ChatInfoItem.Option.SharedGroups,
                ChatInfoItem.Separator,
                ChatInfoItem.Option.DeleteConversation,
            )
        )
    }
    channelDeletedState.observe(lifecycleOwner) { isDeleted ->
        if (isDeleted) {
            view.navigateUpToHome()
        }
    }
}
