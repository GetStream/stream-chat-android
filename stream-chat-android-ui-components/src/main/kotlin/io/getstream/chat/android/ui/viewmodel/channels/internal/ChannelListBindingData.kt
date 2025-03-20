package io.getstream.chat.android.ui.viewmodel.channels.internal

import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel

internal data class ChannelListBindingData(
        val state: ChannelListViewModel.State = ChannelListViewModel.State(false, emptyList()),
        val paginationState: ChannelListViewModel.PaginationState = ChannelListViewModel.PaginationState(),
        val typingEvents: Map<String, TypingEvent> = emptyMap(),
        val draftMessages: Map<String, DraftMessage> = emptyMap(),
    )