package io.getstream.chat.android.compose.ui.chats

import android.content.Context
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * A lambda function that provides a [MessagesViewModelFactory] for managing messages within a selected channel.
 */
public typealias MessagesViewModelFactoryProvider =
        (context: Context, selection: MessageSelection) -> MessagesViewModelFactory?
