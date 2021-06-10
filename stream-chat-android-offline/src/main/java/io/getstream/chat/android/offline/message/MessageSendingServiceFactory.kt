package io.getstream.chat.android.offline.message

import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController

internal class MessageSendingServiceFactory {
    fun create(
        domainImpl: ChatDomainImpl,
        channelController: ChannelController,
        channelClient: ChannelClient,
    ): MessageSendingService = MessageSendingService(domainImpl, channelController, channelClient)
}
