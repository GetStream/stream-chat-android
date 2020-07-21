package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.okhttp.OkHttpSocketFactory
import io.getstream.chat.android.client.token.TokenManagerImpl
import org.junit.Test
import java.lang.Thread.sleep

class TestChatSocketService {

    private val parser = ChatParserImpl()
    private val tokenManager = TokenManagerImpl()

    @Test
    fun test() {
        val eventsParser = EventsParser(parser)
        val service =
            ChatSocketServiceImpl(eventsParser, tokenManager, OkHttpSocketFactory(eventsParser, parser, tokenManager))

        val listenerThread = Thread {
            var n = 200
            while (n > 0) {
                service.addListener(FakeListener())
                sleep((10 * Math.random()).toLong())
                n--
            }
        }

        val eventThread = Thread {
            var n = 200
            while (n > 0) {
                service.onEvent(ChatEvent(EventType.HEALTH_CHECK))
                sleep((10 * Math.random()).toLong())
                n--
            }
        }

        eventThread.start()
        listenerThread.start()

        eventThread.join()
        listenerThread.join()
    }

    private class FakeListener : SocketListener()
}
