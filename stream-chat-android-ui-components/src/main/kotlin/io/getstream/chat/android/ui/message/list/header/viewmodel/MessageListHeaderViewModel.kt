package io.getstream.chat.android.ui.message.list.header.viewmodel

import com.getstream.sdk.chat.viewmodel.BaseMessageListHeaderViewModel
import io.getstream.chat.android.client.ChatClient

/**
 * An implementation of [BaseMessageListHeaderViewModel].
 *
 * @param cid The CID of the current channel.
 * @param chatClient An instance of the low level chat client.
 */
public class MessageListHeaderViewModel(
    cid: String,
    chatClient: ChatClient = ChatClient.instance(),
) : BaseMessageListHeaderViewModel(cid, chatClient)
