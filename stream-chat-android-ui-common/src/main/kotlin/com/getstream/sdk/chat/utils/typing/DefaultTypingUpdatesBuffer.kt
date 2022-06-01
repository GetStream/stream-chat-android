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

package com.getstream.sdk.chat.utils.typing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * After you call [keystroke] the class will call [onStartTypingEventFired],
 * and after buffering for [delayIntervalMillis] it will call [onStopTypingEventFired].
 *
 * Every subsequent keystroke will cancel the previous work
 * and reset the time before sending a stop typing event so that only
 * distinct events trigger calls.
 *
 * @param delayIntervalMillis The interval between calling [onStartTypingEventFired]
 * and [onStopTypingEventFired] whenever a keystroke has been received and buffered.
 * @param onStartTypingEventFired Signals that a typing event should be sent.
 * Usually used to make an API call using [io.getstream.chat.android.client.ChatClient.keystroke]
 * @param onStopTypingEventFired Signals that a stop typing event should be sent.
 * Usually used to make an API call using [io.getstream.chat.android.client.ChatClient.stopTyping]
 */
private class DefaultTypingUpdatesBuffer(
    val delayIntervalMillis: Long = DEFAULT_TYPING_UPDATES_BUFFER_INTERVAL,
    val onStartTypingEventFired: () -> Unit,
    val onStopTypingEventFired: () -> Unit,
) : TypingUpdatesBuffer {

    /**
     * The coroutine scope used for running the times.
     */
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Holds the currently running job.
     */
    var job: Job? = null

    /**
     * If the user is currently typing or not.
     *
     * Sends out a typing related event on every value
     * change.
     */
    private var isTyping: Boolean = false
        set(value) {
            field = value
            handleTypingEvent(isTyping)
        }

    /**
     * Used to send a stop typing event after a
     * set amount of time dictated by [delayIntervalMillis].
     */
    private suspend fun startTypingTimer() {
        delay(delayIntervalMillis)
        clearTypingUpdates()
    }

    /**
     * Sets the value of [isTyping] only if there is
     * a change in state in order to not create unnecessary events.
     *
     * It also resets the job to stop typing events after delay, debouncing keystrokes.
     */
    override fun keystroke() {
        if (!isTyping) {
            isTyping = true
        }
        job?.cancel()
        job = coroutineScope.launch { startTypingTimer() }
    }

    /**
     * Sets [isTyping] to false.
     *
     * Useful for clearing the state manually on lifecycle events.
     */
    override fun clearTypingUpdates() {
        isTyping = false
    }

    /**
     * Calls [onStartTypingEventFired] or [onStopTypingEventFired] event depending on the value of [isTyping].
     *
     * @param isTyping If the user is currently typing.
     */
    private fun handleTypingEvent(isTyping: Boolean) {
        if (isTyping) {
            onStartTypingEventFired()
        } else {
            onStopTypingEventFired()
        }
    }

    companion object {
        const val DEFAULT_TYPING_UPDATES_BUFFER_INTERVAL = 3000L
    }
}
