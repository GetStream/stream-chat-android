package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class HideChannelListenerStateTest {

    private val stateLogic: ChannelStateLogic = mock()
    private val channelLogic: ChannelLogic = mock {
        on(it.stateLogic()) doReturn stateLogic
    }
    private val logicRegistry: LogicRegistry = mock {
        on(it.channel(any(), any())) doReturn channelLogic
    }
    private val hideChannelListenerState = HideChannelListenerState(logicRegistry)

    @Test
    fun `before the request is made, the channel should be set to hidden`() = runTest {
        hideChannelListenerState.onHideChannelRequest(randomString(), randomString(), randomBoolean())

        verify(stateLogic).setHidden(true)
    }

    @Test
    fun `after the request is made and it fails, the channel should be set to NOT hidden`() = runTest {
        hideChannelListenerState.onHideChannelResult(
            Result.error(ChatError()),
            randomString(),
            randomString(),
            randomBoolean()
        )

        verify(stateLogic).setHidden(false)
    }

    @Test
    fun `after the request successful and clear history is true, history should be clean`() = runTest {
        hideChannelListenerState.onHideChannelResult(
            Result.success(Unit),
            randomString(),
            randomString(),
            clearHistory = true
        )

        verify(stateLogic, never()).setHidden(false)
        verify(stateLogic).run {
            hideMessagesBefore(any())
            removeMessagesBefore(any(), any())
        }
    }
}
