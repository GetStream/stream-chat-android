package io.getstream.chat.android.offline.experimental.plugin.factory

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.Config
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.experimental.plugin.listener.ChannelMarkReadListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.EditMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.GetMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.HideChannelListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.MarkAllReadListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.QueryChannelListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.QueryChannelsListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.ThreadQueryListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

@ExperimentalStreamChatApi
public class OfflinePluginFactory {

    public fun create(config: Config): OfflinePlugin {
        val chatDomain = ChatDomain.instance as ChatDomainImpl
        val chatClient = ChatClient.instance()
        val stateRegistry = StateRegistry(chatDomain, chatClient)
        val logic = LogicRegistry(stateRegistry)
        val globalStateRegistry = GlobalMutableState()

        return OfflinePlugin(
            queryChannelsListener = QueryChannelsListenerImpl(logic),
            queryChannelListener = QueryChannelListenerImpl(logic),
            threadQueryListener = ThreadQueryListenerImpl(logic),
            channelMarkReadListener = ChannelMarkReadListenerImpl(logic),
            editMessageListener = EditMessageListenerImpl(logic, globalStateRegistry),
            getMessageListener = GetMessageListenerImpl(logic),
            hideChannelListener = HideChannelListenerImpl(logic),
            markAllReadListener = MarkAllReadListenerImpl(logic),
            config = config
        )
    }
}
