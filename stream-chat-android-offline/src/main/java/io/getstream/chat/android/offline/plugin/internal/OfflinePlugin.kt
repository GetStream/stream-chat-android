/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.plugin.internal

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.DependencyResolver
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.client.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.plugin.listeners.TypingEventListener
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.state.plugin.internal.StateAwarePlugin
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import kotlin.reflect.KClass

/**
 * Implementation of [Plugin] that brings support for the offline feature. This class work as a delegator of calls for one
 * of its dependencies, so avoid to add logic here.
 *
 * @param queryChannelsListener [QueryChannelsListener]
 * @param queryChannelListener [QueryChannelListener]
 * @param threadQueryListener [ThreadQueryListener]
 * @param channelMarkReadListener [ChannelMarkReadListener]
 * @param editMessageListener [EditMessageListener]
 * @param hideChannelListener [HideChannelListener]
 * @param markAllReadListener [MarkAllReadListener]
 * @param deleteReactionListener [DeleteReactionListener]
 * @param sendReactionListener [SendReactionListener]
 * @param deleteMessageListener [DeleteMessageListener]
 * @param sendGiphyListener [SendGiphyListener]
 * @param shuffleGiphyListener [ShuffleGiphyListener]
 * @param sendMessageListener [SendMessageListener]
 * @param queryMembersListener [QueryMembersListener]
 * @param typingEventListener [TypingEventListener]
 * @param createChannelListener [CreateChannelListener]
 * @param getMessageListener [GetMessageListener]
 * @param activeUser User associated with [OfflinePlugin] instance.
 * @param provideDependency Resolves dependency within [OfflinePlugin].
 * @param childResolver Resolves dependency within [StatePlugin]. Will be removed when [StatePlugin]
 * gets separated from [OfflinePlugin].
 */
@Suppress("LongParameterList")
internal class OfflinePlugin(
    internal val activeUser: User,
    private val queryChannelsListener: QueryChannelsListener,
    private val queryChannelListener: QueryChannelListener,
    private val threadQueryListener: ThreadQueryListener,
    private val channelMarkReadListener: ChannelMarkReadListener,
    private val editMessageListener: EditMessageListener,
    private val hideChannelListener: HideChannelListener,
    private val markAllReadListener: MarkAllReadListener,
    private val deleteReactionListener: DeleteReactionListener,
    private val sendReactionListener: SendReactionListener,
    private val deleteMessageListener: DeleteMessageListener,
    private val sendGiphyListener: SendGiphyListener,
    private val shuffleGiphyListener: ShuffleGiphyListener,
    private val sendMessageListener: SendMessageListener,
    private val queryMembersListener: QueryMembersListener,
    private val typingEventListener: TypingEventListener,
    private val createChannelListener: CreateChannelListener,
    private val getMessageListener: GetMessageListener,
    private val fetchCurrentUserListener: FetchCurrentUserListener,
    @Deprecated("Delete this when StatePlugin will be separated from OfflinePlugin")
    private val childResolver: DependencyResolver,
    private val provideDependency: (KClass<*>) -> Any? = { null },
) : StateAwarePlugin,
    DependencyResolver,
    QueryChannelsListener by queryChannelsListener,
    QueryChannelListener by queryChannelListener,
    ThreadQueryListener by threadQueryListener,
    ChannelMarkReadListener by channelMarkReadListener,
    EditMessageListener by editMessageListener,
    HideChannelListener by hideChannelListener,
    MarkAllReadListener by markAllReadListener,
    DeleteReactionListener by deleteReactionListener,
    SendReactionListener by sendReactionListener,
    DeleteMessageListener by deleteMessageListener,
    SendGiphyListener by sendGiphyListener,
    ShuffleGiphyListener by shuffleGiphyListener,
    SendMessageListener by sendMessageListener,
    QueryMembersListener by queryMembersListener,
    TypingEventListener by typingEventListener,
    CreateChannelListener by createChannelListener,
    GetMessageListener by getMessageListener,
    FetchCurrentUserListener by fetchCurrentUserListener {

    override val name: String = MODULE_NAME

    @Suppress("UNCHECKED_CAST")
    @InternalStreamChatApi
    public override fun <T : Any> resolveDependency(klass: KClass<T>): T? = provideDependency(klass) as? T
        ?: childResolver.resolveDependency(klass)

    private companion object {
        /**
         * Name of this plugin module.
         */
        private const val MODULE_NAME: String = "Offline"
    }
}
