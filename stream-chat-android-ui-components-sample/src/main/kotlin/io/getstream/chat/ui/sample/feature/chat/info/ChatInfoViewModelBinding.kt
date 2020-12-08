package io.getstream.chat.ui.sample.feature.chat.info

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.subscribeFor

fun ChatInfoViewModel.bindView(view: ChatInfoFragment, lifecycleOwner: LifecycleOwner) {
    view.setChatInfoStatefulOptionChangedListener { option, isChecked ->
        when (option) {
            is ChatInfoItem.Option.Stateful.Notifications -> onEvent(
                ChatInfoViewModel.Event.OptionNotificationClicked(isChecked)
            )
            is ChatInfoItem.Option.Stateful.Mute -> onEvent(
                ChatInfoViewModel.Event.OptionMuteUserClicked(isChecked)
            )
            is ChatInfoItem.Option.Stateful.Block -> onEvent(
                ChatInfoViewModel.Event.OptionBlockUserClicked(isChecked)
            )
        }
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
                ChatInfoItem.Option.Stateful.Mute(isChecked = state.isMemberMuted),
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
