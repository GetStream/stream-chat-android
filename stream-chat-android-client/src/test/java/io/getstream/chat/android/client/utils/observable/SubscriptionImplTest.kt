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
import io.getstream.chat.android.client.events.ChatEvent
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

internal class SubscriptionImplTest {

    @Test
    fun `onNext should deliver event when filter returns true`() {
        val mockListener = mock<ChatEventListener<ChatEvent>>()
        val subscription = SubscriptionImpl(
            filter = { true },
            listener = mockListener,
        )

        val event = mock<ChatEvent>()
        subscription.onNext(event)

        verify(mockListener).onEvent(event)
    }

    @Test
    fun `onNext should not deliver event when filter returns false`() {
        val mockListener = mock<ChatEventListener<ChatEvent>>()
        val subscription = SubscriptionImpl(
            filter = { false },
            listener = mockListener,
        )

        val event = mock<ChatEvent>()
        subscription.onNext(event)

        verify(mockListener, never()).onEvent(event)
    }

    @Test
    fun `afterEventDelivered should be called after successful event delivery`() {
        val mockListener = mock<ChatEventListener<ChatEvent>>()
        var afterDeliveryCalled = false
        val subscription = SubscriptionImpl(
            filter = { true },
            listener = mockListener,
        ).apply {
            afterEventDelivered = { afterDeliveryCalled = true }
        }

        val event = mock<ChatEvent>()
        subscription.onNext(event)

        assertTrue(afterDeliveryCalled)
    }

    @Test
    fun `afterEventDelivered should be called even if listener throws exception`() {
        val mockListener = mock<ChatEventListener<ChatEvent>>()
        whenever(mockListener.onEvent(any())).thenThrow(RuntimeException())
        var afterDeliveryCalled = false
        val subscription = SubscriptionImpl(
            filter = { true },
            listener = mockListener,
        ).apply {
            afterEventDelivered = { afterDeliveryCalled = true }
        }

        val event = mock<ChatEvent>()
        assertThrows<RuntimeException> {
            subscription.onNext(event)
        }

        assertTrue(afterDeliveryCalled)
    }

    @Test
    fun `dispose should set isDisposed to true`() {
        val subscription = SubscriptionImpl(
            filter = { true },
            listener = mock(),
        )

        subscription.dispose()

        assertTrue(subscription.isDisposed)
    }

    @Test
    fun `dispose should clear listener reference`() {
        val mockListener = mock<ChatEventListener<ChatEvent>>()
        val subscription = SubscriptionImpl(
            filter = { true },
            listener = mockListener,
        )

        subscription.dispose()
        subscription.isDisposed = false

        val event = mock<ChatEvent>()
        subscription.onNext(event)

        verify(mockListener, never()).onEvent(event)
    }

    @Test
    fun `onNext should throw IllegalStateException if subscription is already disposed`() {
        val subscription = SubscriptionImpl(
            filter = { true },
            listener = mock(),
        ).apply {
            dispose()
        }

        val event = mock<ChatEvent>()

        assertThrows<IllegalStateException> {
            subscription.onNext(event)
        }
    }

    @Test
    fun `onNext should not call listener if disposed concurrently`() {
        val gate = CompletableFuture<Unit>() // blocks the filter
        val filterEntered = CompletableFuture<Unit>() // signals we are inside the filter

        val mockListener = mock<ChatEventListener<ChatEvent>>()
        val subscription = SubscriptionImpl(
            filter = {
                filterEntered.complete(Unit) // tell the test we are here
                gate.get() // wait until the test lets us go
                true
            },
            listener = mockListener,
        )

        val event = mock<ChatEvent>()
        val exceptions = mutableListOf<Throwable>()

        val onNextFuture = CompletableFuture.runAsync {
            try {
                subscription.onNext(event)
            } catch (e: Throwable) {
                exceptions.add(e)
            }
        }

        // Wait until the filter is entered (ensures onNext is truly paused)
        filterEntered.get(1, TimeUnit.SECONDS)

        subscription.dispose() //  Dispose from the test thread – this is the concurrent part

        gate.complete(Unit) // Unblock the filter so onNext can finish its execution

        // Verify the outcome (no exception, listener never called)
        onNextFuture.get(1, TimeUnit.SECONDS)
        assertEquals(0, exceptions.size)
        verify(mockListener, never()).onEvent(event)
    }
}
