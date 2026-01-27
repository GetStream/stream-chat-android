/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.offline.plugin

import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageForMeListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.DraftMessageListener
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.client.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.QueryThreadsListener
import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User
import kotlin.reflect.KClass

/**
 * Implementation of [Plugin] that brings support for the offline feature. This class work as a delegator of calls for
 * one of its dependencies, so avoid to add logic here.
 *
 * @param activeUser User associated with [OfflinePlugin] instance.
 * @param queryChannelListener [QueryChannelListener]
 * @param threadQueryListener [ThreadQueryListener]
 * @param editMessageListener [EditMessageListener]
 * @param hideChannelListener [HideChannelListener]
 * @param deleteReactionListener [DeleteReactionListener]
 * @param sendReactionListener [SendReactionListener]
 * @param deleteMessageListener [DeleteMessageListener]
 * @param deleteMessageForMeListener [DeleteMessageForMeListener]
 * @param shuffleGiphyListener [ShuffleGiphyListener]
 * @param sendMessageListener [SendMessageListener]
 * @param sendAttachmentListener [SendAttachmentListener]
 * @param queryMembersListener [QueryMembersListener]
 * @param createChannelListener [CreateChannelListener]
 * @param deleteChannelListener [DeleteChannelListener]
 * @param getMessageListener [GetMessageListener]
 * @param fetchCurrentUserListener [FetchCurrentUserListener]
 * @param queryThreadsListener [QueryThreadsListener]
 * @param draftMessageListener [DraftMessageListener]
 * @param provideDependency Resolves dependency within [OfflinePlugin].
 */
@Suppress("LongParameterList")
internal class OfflinePlugin(
    internal val activeUser: User,
    private val queryChannelListener: QueryChannelListener,
    private val threadQueryListener: ThreadQueryListener,
    private val editMessageListener: EditMessageListener,
    private val hideChannelListener: HideChannelListener,
    private val deleteReactionListener: DeleteReactionListener,
    private val sendReactionListener: SendReactionListener,
    private val deleteMessageListener: DeleteMessageListener,
    private val deleteMessageForMeListener: DeleteMessageForMeListener,
    private val shuffleGiphyListener: ShuffleGiphyListener,
    private val sendMessageListener: SendMessageListener,
    private val sendAttachmentListener: SendAttachmentListener,
    private val queryMembersListener: QueryMembersListener,
    private val createChannelListener: CreateChannelListener,
    private val deleteChannelListener: DeleteChannelListener,
    private val getMessageListener: GetMessageListener,
    private val fetchCurrentUserListener: FetchCurrentUserListener,
    private val queryThreadsListener: QueryThreadsListener,
    private val draftMessageListener: DraftMessageListener,
    private val provideDependency: (KClass<*>) -> Any? = { null },
) : Plugin,
    QueryChannelListener by queryChannelListener,
    ThreadQueryListener by threadQueryListener,
    EditMessageListener by editMessageListener,
    HideChannelListener by hideChannelListener,
    DeleteReactionListener by deleteReactionListener,
    SendReactionListener by sendReactionListener,
    DeleteMessageListener by deleteMessageListener,
    DeleteMessageForMeListener by deleteMessageForMeListener,
    ShuffleGiphyListener by shuffleGiphyListener,
    SendMessageListener by sendMessageListener,
    QueryMembersListener by queryMembersListener,
    CreateChannelListener by createChannelListener,
    DeleteChannelListener by deleteChannelListener,
    SendAttachmentListener by sendAttachmentListener,
    GetMessageListener by getMessageListener,
    FetchCurrentUserListener by fetchCurrentUserListener,
    QueryThreadsListener by queryThreadsListener,
    DraftMessageListener by draftMessageListener {

    override fun onUserSet(user: User) {
        /* No-Op */
    }

    override fun onUserDisconnected() {
        /* No-Op */
    }

    @Suppress("UNCHECKED_CAST")
    @InternalStreamChatApi
    override fun <T : Any> resolveDependency(klass: KClass<T>): T? = provideDependency(klass) as? T
}
