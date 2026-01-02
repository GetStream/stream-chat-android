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

package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.EventType
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date

internal class ChatEventsObservable(
    private val waitConnection: FlowCollector<Result<ConnectionData>>,
    private val scope: CoroutineScope,
    private val chatSocket: ChatSocket,
) {

    private val mutex = Mutex()

    private val subscriptions = mutableSetOf<EventSubscription>()
    private val eventsMapper = EventsMapper(this)

    private fun onNext(event: ChatEvent) {
        notifySubscriptions(event)
        emitConnectionEvents(event)
    }

    private fun emitConnectionEvents(event: ChatEvent) {
        scope.launch {
            when (event) {
                is ConnectedEvent -> {
                    waitConnection.emit(Result.Success(ConnectionData(event.me, event.connectionId)))
                }
                is ErrorEvent -> {
                    waitConnection.emit(Result.Failure(event.error))
                }
                is DisconnectedEvent -> {
                    // We can get an unrecoverable error when attempting to connect to the socket
                    // (ex. connection.error event - wrapped in a DisconnectedEvent)
                    val error = (event.disconnectCause as? DisconnectCause.UnrecoverableError)?.error
                    if (error != null) {
                        waitConnection.emit(Result.Failure(error))
                    }
                }
                else -> Unit // Ignore other events
            }
        }
    }

    fun subscribe(
        filter: (ChatEvent) -> Boolean = { true },
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return addSubscription(SubscriptionImpl(filter, listener))
    }

    fun subscribeSuspend(
        filter: (ChatEvent) -> Boolean = { true },
        listener: ChatEventSuspendListener<ChatEvent>,
    ): Disposable {
        return addSubscription(SuspendSubscription(scope, filter, listener))
    }

    fun subscribeSingle(
        filter: (ChatEvent) -> Boolean = { true },
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return addSubscription(
            SubscriptionImpl(filter, listener).apply {
                afterEventDelivered = this::dispose
            },
        )
    }

    private fun notifySubscriptions(event: ChatEvent) {
        scope.launch {
            mutex.withLock {
                val iterator = subscriptions.iterator()
                while (iterator.hasNext()) {
                    val subscription = iterator.next()
                    if (subscription.isDisposed) {
                        iterator.remove()
                    } else {
                        subscription.onNext(event)
                    }
                }
                // remove listener from socket events if there are no subscriptions
                if (subscriptions.isEmpty()) {
                    chatSocket.removeListener(eventsMapper)
                }
            }
        }
    }

    private fun addSubscription(subscription: EventSubscription): Disposable {
        scope.launch {
            mutex.withLock {
                // add listener to socket events only once
                if (subscriptions.isEmpty()) {
                    chatSocket.addListener(eventsMapper)
                }
                subscriptions.add(subscription)
            }
        }
        return subscription
    }

    internal fun interface ChatEventSuspendListener<EventT : ChatEvent> {
        suspend fun onEvent(event: EventT)
    }

    /**
     * Maps methods of [SocketListener] to events of [ChatEventsObservable]
     */
    private class EventsMapper(private val observable: ChatEventsObservable) : SocketListener() {

        override val deliverOnMainThread: Boolean
            get() = false

        override fun onConnecting() {
            observable.onNext(ConnectingEvent(EventType.CONNECTION_CONNECTING, Date(), null))
        }

        override fun onConnected(event: ConnectedEvent) {
            observable.onNext(event)
        }

        override fun onDisconnected(cause: DisconnectCause) {
            observable.onNext(
                DisconnectedEvent(
                    EventType.CONNECTION_DISCONNECTED,
                    createdAt = Date(),
                    disconnectCause = cause,
                    rawCreatedAt = null,
                ),
            )
        }

        override fun onEvent(event: ChatEvent) {
            observable.onNext(event)
        }

        override fun onError(error: Error) {
            observable.onNext(
                ErrorEvent(
                    EventType.CONNECTION_ERROR,
                    createdAt = Date(),
                    error = error,
                    rawCreatedAt = null,
                ),
            )
        }
    }
}
