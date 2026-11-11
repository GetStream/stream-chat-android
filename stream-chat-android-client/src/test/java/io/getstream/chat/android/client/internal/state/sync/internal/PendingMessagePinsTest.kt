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

package io.getstream.chat.android.client.internal.state.sync.internal

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.IOException
import java.util.Date

internal class PendingMessagePinsTest {

    private val me = randomUser(id = "me")
    private val now = Date(10_000)
    private val stored = randomMessage(
        id = MESSAGE_ID,
        parentId = null,
        pinned = false,
        pinnedAt = null,
        pinExpires = null,
        pinnedBy = null,
    )
    private val channelLogic: ChannelLogic = mock()
    private var stateMessage: Message? = stored
    private val logic: LogicRegistry = mock {
        on(it.getMessageById(MESSAGE_ID)) doAnswer { stateMessage }
        on(it.channelFromMessage(any())) doReturn channelLogic
        on(it.getActiveQueryThreadsLogic()) doReturn emptyList()
    }
    private val sent = mutableListOf<Map<String, Any>>()
    private var sendResult: Result<Message> = Result.Success(stored)
    private var isNetworkAvailable = false
    private val pins = PendingMessagePins(
        logic = logic,
        sendPin = { _, set ->
            sent += set
            sendResult
        },
        getCurrentUser = { me },
        isNetworkAvailable = { isNetworkAvailable },
        now = { now },
    )

    @Test
    fun `a pin that times out while the network is available is not kept`() = runTest {
        isNetworkAvailable = true

        pins.onPinResult(stored, pinned = true, pinExpires = null, result = offline())
        pins.sendPending()

        verify(channelLogic, never()).upsertMessage(any())
        sent shouldBeEqualTo emptyList()
    }

    @Test
    fun `a pin that fails offline is shown at once and sent on the next connection`() = runTest {
        val expires = Date(20_000)

        pins.onPinResult(stored, pinned = true, pinExpires = expires, result = offline())

        val shown = upserted().single()
        shown.pinned shouldBeEqualTo true
        shown.pinnedAt shouldBeEqualTo now
        shown.pinExpires shouldBeEqualTo expires
        shown.pinnedBy shouldBeEqualTo me

        pins.sendPending()

        sent shouldBeEqualTo listOf(mapOf("pinned" to true, "pin_expires" to expires))
        pins.sendPending()
        sent.size shouldBeEqualTo 1
    }

    @Test
    fun `an unpin that fails offline clears the pin fields`() = runTest {
        val pinned = stored.copy(pinned = true, pinnedAt = Date(1_000), pinnedBy = randomUser())
        stateMessage = pinned

        pins.onPinResult(pinned, pinned = false, pinExpires = null, result = offline())

        val shown = upserted().single()
        shown.pinned shouldBeEqualTo false
        shown.pinnedAt.shouldBeNull()
        shown.pinnedBy.shouldBeNull()
        pins.sendPending()
        sent shouldBeEqualTo listOf(mapOf<String, Any>("pinned" to false))
    }

    @Test
    fun `only the last pin or unpin of a message is sent`() = runTest {
        pins.onPinResult(stored, pinned = true, pinExpires = null, result = offline())
        pins.onPinResult(stored, pinned = false, pinExpires = null, result = offline())

        pins.sendPending()

        sent shouldBeEqualTo listOf(mapOf<String, Any>("pinned" to false))
    }

    @Test
    fun `a pin that succeeds later drops the one kept before`() = runTest {
        pins.onPinResult(stored, pinned = true, pinExpires = null, result = offline())
        pins.onPinResult(stored, pinned = false, pinExpires = null, result = Result.Success(stored))

        pins.sendPending()

        sent shouldBeEqualTo emptyList()
    }

    @Test
    fun `a pin that fails with another error is not kept`() = runTest {
        pins.onPinResult(stored, pinned = true, pinExpires = null, result = rejected())

        pins.sendPending()

        verify(channelLogic, never()).upsertMessage(any())
        sent shouldBeEqualTo emptyList()
    }

    @Test
    fun `a pin still offline on the next connection is kept`() = runTest {
        pins.onPinResult(stored, pinned = true, pinExpires = null, result = offline())
        sendResult = offline()

        pins.sendPending()
        sendResult = Result.Success(stored)
        pins.sendPending()

        sent.size shouldBeEqualTo 2
        verify(channelLogic, times(1)).upsertMessage(any())
    }

    @Test
    fun `a pin the server rejects on the next connection is dropped and the message shows its state from before`() =
        runTest {
            pins.onPinResult(stored, pinned = true, pinExpires = Date(20_000), result = offline())
            stateMessage = upserted().single()
            sendResult = rejected()

            pins.sendPending()

            val reverted = upserted().last()
            reverted.pinned shouldBeEqualTo false
            reverted.pinnedAt.shouldBeNull()
            reverted.pinExpires.shouldBeNull()
            reverted.pinnedBy.shouldBeNull()
            pins.sendPending()
            sent.size shouldBeEqualTo 1
        }

    @Test
    fun `an unpin made while the kept pin is sent stays kept`() = runTest {
        lateinit var target: PendingMessagePins
        target = PendingMessagePins(
            logic = logic,
            sendPin = { _, set ->
                sent += set
                if (sent.size == 1) {
                    target.onPinResult(stored, pinned = false, pinExpires = null, result = offline())
                }
                Result.Success(stored)
            },
            getCurrentUser = { me },
            isNetworkAvailable = { false },
            now = { now },
        )
        target.onPinResult(stored, pinned = true, pinExpires = null, result = offline())

        target.sendPending()
        target.sendPending()

        sent shouldBeEqualTo listOf(mapOf<String, Any>("pinned" to true), mapOf<String, Any>("pinned" to false))
    }

    private fun upserted(): List<Message> =
        argumentCaptor<Message>().apply { verify(channelLogic, atLeastOnce()).upsertMessage(capture()) }.allValues

    private fun offline(): Result<Message> = Result.Failure(
        Error.NetworkError(
            message = "offline",
            serverErrorCode = ChatErrorCode.NETWORK_FAILED.code,
            statusCode = -1,
            cause = IOException("offline"),
        ),
    )

    private fun rejected(): Result<Message> = Result.Failure(
        Error.NetworkError(
            message = "pin_expires must be in the future",
            serverErrorCode = ChatErrorCode.VALIDATION_ERROR.code,
            statusCode = 400,
        ),
    )

    private companion object {
        const val MESSAGE_ID = "message-id"
    }
}
