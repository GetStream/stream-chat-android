package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.SocketListener
import org.assertj.core.api.Assertions.assertThat

internal class FakeSocketService(
    val eventsCollector: MutableList<ChatEvent> = mutableListOf()
) : ChatSocketService {

    private var connectionUserId: String? = null

    override fun anonymousConnect(endpoint: String, apiKey: String) { }

    override fun userConnect(endpoint: String, apiKey: String, user: User) { }

    private val listeners = mutableListOf<SocketListener>()

    fun sendEvent(event: ChatEvent) {
        listeners.forEach {
            it.onEvent(event)
        }
    }

    override fun disconnect() {
    }

    override fun addListener(listener: SocketListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SocketListener) {
        listeners.remove(listener)
    }

    override fun onSocketError(error: ChatError) {
    }

    override fun onConnectionResolved(event: ConnectedEvent) {
        connectionUserId = event.me.id
    }

    override fun onEvent(event: ChatEvent) {
        eventsCollector.add(event)
    }

    fun verifyConnectionUserId(userId: String) {
        assertThat(userId).isEqualTo(connectionUserId)
    }

    fun verifyNoConnectionUserId() {
        assertThat(connectionUserId).isNull()
    }
}
