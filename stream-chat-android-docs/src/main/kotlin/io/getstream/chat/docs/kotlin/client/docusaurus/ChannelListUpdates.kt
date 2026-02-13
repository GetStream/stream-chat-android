@file:Suppress("unused")

package io.getstream.chat.docs.kotlin.client.docusaurus

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.client.api.event.ChatEventHandler
import io.getstream.chat.android.client.api.event.DefaultChatEventHandler
import io.getstream.chat.android.client.api.event.EventHandlingResult
import io.getstream.chat.android.client.api.event.ChatEventHandlerFactory
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import kotlinx.coroutines.flow.StateFlow

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/channel-list-updates/">Channel List Updates</a>
 */
class ChannelListUpdates {

    fun publicChannelsChatEventHandler() {
        class PublicChatEventHandler(
            channels: StateFlow<Map<String, Channel>?>,
            clientState: ClientState,
        ) : DefaultChatEventHandler(channels, clientState) {

            override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
                // If the channel event matches "public" type, handle it
                return if (event.channel.cid.startsWith("public")) {
                    super.handleChannelEvent(event, filter)
                } else {
                    // Otherwise skip
                    EventHandlingResult.Skip
                }
            }

            override fun handleCidEvent(
                event: CidEvent,
                filter: FilterObject,
                cachedChannel: Channel?,
            ): EventHandlingResult {
                // If the cid event matches "public" type, handle it
                return if (event.cid.startsWith("public")) {
                    super.handleCidEvent(event, filter, cachedChannel)
                } else {
                    // Otherwise skip
                    EventHandlingResult.Skip
                }
            }
        }

        class PublicChatEventHandlerFactory : ChatEventHandlerFactory() {
            override fun chatEventHandler(channels: StateFlow<Map<String, Channel>?>): ChatEventHandler {
                val chatClient = ChatClient.instance()
                return PublicChatEventHandler(channels, chatClient.clientState)
            }
        }
    }

    fun privateChannelsChatEventHandler() {
        class PrivateChatEventHandler(
            channels: StateFlow<Map<String, Channel>?>,
            clientState: ClientState,
        ) : DefaultChatEventHandler(channels, clientState) {

            override fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
                // If the channel event matches "private" type, handle it
                return if (event.channel.cid.startsWith("private")) {
                    super.handleChannelEvent(event, filter)
                } else {
                    // Otherwise skip
                    EventHandlingResult.Skip
                }
            }

            override fun handleCidEvent(
                event: CidEvent,
                filter: FilterObject,
                cachedChannel: Channel?,
            ): EventHandlingResult {
                // If the cid event matches "private" type, handle it
                return if (event.cid.startsWith("private")) {
                    super.handleCidEvent(event, filter, cachedChannel)
                } else {
                    // Otherwise skip
                    EventHandlingResult.Skip
                }
            }
        }

        class PrivateChatEventHandlerFactory : ChatEventHandlerFactory() {
            override fun chatEventHandler(channels: StateFlow<Map<String, Channel>?>): ChatEventHandler {
                val chatClient = ChatClient.instance()
                return PrivateChatEventHandler(channels, chatClient.clientState)
            }
        }
    }

    fun applyToViewModel(chatEventHandlerFactory: ChatEventHandlerFactory) {
        val factory = ChannelListViewModelFactory(chatEventHandlerFactory = chatEventHandlerFactory)
    }
}
