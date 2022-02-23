package io.getstream.chat.android.offline.experimental.plugin

import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.experimental.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.experimental.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Implementation of [Plugin] that brings support for the offline feature. This class work as a delegator of calls for one
 * of its dependencies, so avoid to add logic here.
 *
 * @param queryChannelsListener [QueryChannelsListener]
 * @param queryChannelListener [QueryChannelListener]
 * @param threadQueryListener [ThreadQueryListener]
 * @param channelMarkReadListener [ChannelMarkReadListener]
 * @param editMessageListener [EditMessageListener]
 * @param getMessageListener [GetMessageListener]
 * @param hideChannelListener [HideChannelListener]
 * @param markAllReadListener [MarkAllReadListener]
 * @param deleteReactionListener [DeleteReactionListener]
 * @param sendReactionListener [SendReactionListener]
 */
@InternalStreamChatApi
@ExperimentalStreamChatApi
internal class OfflinePlugin(
    private val queryChannelsListener: QueryChannelsListener,
    private val queryChannelListener: QueryChannelListener,
    private val threadQueryListener: ThreadQueryListener,
    private val channelMarkReadListener: ChannelMarkReadListener,
    private val editMessageListener: EditMessageListener,
    private val getMessageListener: GetMessageListener,
    private val hideChannelListener: HideChannelListener,
    private val markAllReadListener: MarkAllReadListener,
    private val deleteReactionListener: DeleteReactionListener,
    private val sendReactionListener: SendReactionListener,
) : Plugin,
    QueryChannelsListener by queryChannelsListener,
    QueryChannelListener by queryChannelListener,
    ThreadQueryListener by threadQueryListener,
    ChannelMarkReadListener by channelMarkReadListener,
    EditMessageListener by editMessageListener,
    GetMessageListener by getMessageListener,
    HideChannelListener by hideChannelListener,
    MarkAllReadListener by markAllReadListener,
    DeleteReactionListener by deleteReactionListener,
    SendReactionListener by sendReactionListener {

    override val name: String = MODULE_NAME

    private companion object {
        /**
         * Name of this plugin module.
         */
        private const val MODULE_NAME: String = "Offline"
    }
}
