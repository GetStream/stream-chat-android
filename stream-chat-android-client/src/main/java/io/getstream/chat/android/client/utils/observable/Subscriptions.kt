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

package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.events.ChatEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public interface Disposable {
    public val isDisposed: Boolean
    public fun dispose()
}

internal interface EventSubscription : Disposable {
    fun onNext(event: ChatEvent)
}

internal open class SubscriptionImpl(
    private val filter: (ChatEvent) -> Boolean,
    listener: ChatEventListener<ChatEvent>,
) : EventSubscription {

    @Volatile
    private var listener: ChatEventListener<ChatEvent>? = listener

    @Volatile
    override var isDisposed: Boolean = false

    var afterEventDelivered: () -> Unit = {}

    override fun dispose() {
        isDisposed = true
        listener = null
    }

    final override fun onNext(event: ChatEvent) {
        check(!isDisposed) { "Subscription already disposed, onNext should not be called on it" }

        if (filter(event)) {
            try {
                listener?.onEvent(event)
            } finally {
                afterEventDelivered()
            }
        }
    }
}

internal class SuspendSubscription(
    private val scope: CoroutineScope,
    private val filter: (ChatEvent) -> Boolean,
    listener: ChatEventsObservable.ChatEventSuspendListener<ChatEvent>,
) : EventSubscription {
    @Volatile
    private var listener: ChatEventsObservable.ChatEventSuspendListener<ChatEvent>? = listener

    @Volatile
    override var isDisposed: Boolean = false

    override fun dispose() {
        isDisposed = true
        listener = null
    }

    override fun onNext(event: ChatEvent) {
        check(!isDisposed) { "Subscription already disposed, onNext should not be called on it" }
        scope.launch {
            if (filter(event)) {
                listener?.onEvent(event)
            }
        }
    }
}
