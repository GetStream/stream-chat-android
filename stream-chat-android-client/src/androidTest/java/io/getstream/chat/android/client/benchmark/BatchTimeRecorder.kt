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

package io.getstream.chat.android.client.benchmark

import android.os.SystemClock
import android.util.Log
import io.getstream.log.IsLoggableValidator
import io.getstream.log.Priority
import io.getstream.log.StreamLog
import io.getstream.log.StreamLogger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Times each event batch of the state plugin from the two log lines around it, so the SDK needs no change to be
 * measured. Only those lines and errors are logged: logging every event would add its own cost to the numbers.
 *
 * @param eventType Only batches with an event of this type are recorded, to leave out the ones of other events.
 */
internal class BatchTimeRecorder(private val eventType: String) : StreamLogger, IsLoggableValidator {

    /** One handled batch: its number of events and the time from its start to its end, in nanoseconds. */
    data class Batch(val size: Int, val nanos: Long)

    private val started = ConcurrentHashMap<String, Pair<Int, Long>>()

    val batches = CopyOnWriteArrayList<Batch>()

    /** Errors the SDK logged, which make a run invalid: a payload it could not parse is not handled at all. */
    val errors = CopyOnWriteArrayList<String>()

    fun install() {
        StreamLog.unInstall()
        StreamLog.setValidator(this)
        StreamLog.install(this)
    }

    fun uninstall() {
        StreamLog.unInstall()
    }

    fun reset() {
        started.clear()
        batches.clear()
    }

    override fun isLoggable(priority: Priority, tag: String): Boolean =
        tag == EVENT_HANDLER_TAG || priority.level >= Priority.ERROR.level

    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {
        val now = SystemClock.elapsedRealtimeNanos()
        if (priority.level >= Priority.ERROR.level) {
            errors += "$tag: $message${throwable?.let { " ($it)" }.orEmpty()}"
            Log.e(tag, message, throwable)
            return
        }
        batchStart.find(message)?.let { match ->
            val (id, size, types) = match.destructured
            if (eventType in types) started[id] = size.toInt() to now
            return
        }
        batchEnd.find(message)?.let { match ->
            started.remove(match.groupValues[1])?.let { (size, startedAt) -> batches += Batch(size, now - startedAt) }
        }
    }

    private companion object {
        private const val EVENT_HANDLER_TAG = "Chat:EventHandlerSeq"
        private val batchStart = Regex("""^\[handleBatchEvent] >>> id: (\S+), fromSocket: \S+, size: (\d+), event.types: '(.*)'""")
        private val batchEnd = Regex("""^\[handleBatchEvent] <<< id: (\S+)""")
    }
}
