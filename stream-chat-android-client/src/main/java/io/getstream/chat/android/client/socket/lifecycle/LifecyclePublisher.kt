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

package io.getstream.chat.android.client.socket.lifecycle

import io.getstream.chat.android.client.socket.Event
import io.getstream.chat.android.client.socket.Timed
import io.getstream.chat.android.client.socket.isStopped
import io.getstream.chat.android.client.socket.isStoppedAndAborted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach

internal interface LifecyclePublisher {
    val lifecycleEvents: Flow<Timed<Event.Lifecycle>>

    fun observe()

    fun dispose()
}

internal fun List<LifecyclePublisher>.combine(): Flow<Event.Lifecycle> {
    return combine(this.map { it.lifecycleEvents }) {
        listOf(*it).combineLifecycleEvents()
    }
        .distinctUntilChanged { old, new -> old == new || (old.isStopped() && new.isStopped()) }
        .onEach {
            println("Filtered lifecycle event $it")
        }
}

private fun List<Timed<Event.Lifecycle>>.combineLifecycleEvents() =
    if (any { it.value.isStoppedAndAborted() }) {
        filter { it.value.isStoppedAndAborted() }
            .minByOrNull { it.time }!!
            .value
    } else if (any { it.value.isStopped() }) {
        filter { it.value.isStopped() }
            .minByOrNull { it.time }!!
            .value
    } else Event.Lifecycle.Started
