package io.getstream.chat.ui.sample.feature.chat.info

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.subscribeFor
import java.lang.IllegalStateException

fun ChatInfoViewModel.bindView(view: ChatInfoFragment, lifecycleOwner: LifecycleOwner) {
    view.setChatInfoStatefulOptionChangedListener { option, isChecked ->
        onEvent(
            when (option.optionType) {
                OptionType.NOTIFICATIONS -> ChatInfoViewModel.Event.OptionNotificationClicked(isChecked)
                OptionType.MUTE -> ChatInfoViewModel.Event.OptionMuteUserClicked(isChecked)
                OptionType.BLOCK -> ChatInfoViewModel.Event.OptionBlockUserClicked(isChecked)
                else -> throw IllegalStateException("Chat info option ${option.optionType} is not supported!")
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
                ChatInfoItem.MemberItem(state.chatMember),
                ChatInfoItem.Separator,
                ChatInfoItem.StatefulOption(OptionType.NOTIFICATIONS, isChecked = state.notificationsEnabled),
                ChatInfoItem.StatefulOption(OptionType.MUTE, isChecked = state.isMemberMuted),
                ChatInfoItem.StatefulOption(OptionType.BLOCK, isChecked = state.isMemberBlocked),
                ChatInfoItem.Option(OptionType.SHARED_MEDIA),
                ChatInfoItem.Option(OptionType.SHARED_FILES),
                ChatInfoItem.Option(OptionType.SHARED_GROUPS),
                ChatInfoItem.Separator,
                ChatInfoItem.Option(OptionType.DELETE_CONVERSATION),
            )
        )
    }
    channelDeletedState.observe(lifecycleOwner) { isDeleted ->
        if (isDeleted) {
            view.navigateUpToHome()
        }
    }
}
