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

package io.getstream.chat.android.client.internal.state.plugin.state.internal

import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

/**
 * A [StateFlow] wrapper returned by [io.getstream.chat.android.client.api.state.queryChannelsAsState].
 * Identifies a channel list by [identifier]; used as a weak key in
 * [io.getstream.chat.android.client.api.state.StateRegistry]'s tracker so the entry is evicted
 * automatically once the caller releases the returned flow.
 *
 * The delegate is created in the constructor from [createDelegate], which receives a weak reference to this
 * flow. The delegate can start producing state before the constructor returns, so this lets the producer
 * register the flow without a race and without keeping it alive.
 *
 * Code reached from [createDelegate] can run before the constructor returns, so it may read [identifier] only.
 * Reading [value] or collecting this flow there fails, because the delegate is not assigned yet.
 *
 * @param identifier The identifier of the channel list.
 * @param scopeJob The [Job] of the scope the flow was created in. Once it is no longer active, for example when a
 * screen replaces its query, the flow no longer counts as open. Null counts as always active.
 * @param createDelegate Creates the delegate from a weak reference to this flow.
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
internal class TrackedQueryChannelsStateFlow(
    val identifier: QueryChannelsIdentifier,
    private val scopeJob: Job? = null,
    createDelegate: (WeakReference<TrackedQueryChannelsStateFlow>) -> StateFlow<QueryChannelsState?>,
) : StateFlow<QueryChannelsState?> {

    private val delegate: StateFlow<QueryChannelsState?> = createDelegate(WeakReference(this))

    /** Whether the scope the flow was created in is still active. */
    val isActive: Boolean
        get() = scopeJob?.isActive ?: true

    override val value: QueryChannelsState?
        get() = delegate.value

    override val replayCache: List<QueryChannelsState?>
        get() = delegate.replayCache

    override suspend fun collect(collector: FlowCollector<QueryChannelsState?>): Nothing =
        delegate.collect(collector)
}
