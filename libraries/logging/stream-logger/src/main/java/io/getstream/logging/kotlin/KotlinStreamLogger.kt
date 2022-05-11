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

package io.getstream.logging.kotlin

import io.getstream.logging.Priority
import io.getstream.logging.Priority.ASSERT
import io.getstream.logging.Priority.ERROR
import io.getstream.logging.StreamLogger
import io.getstream.logging.helper.stringify
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * The [StreamLogger] implementation for kotlin projects. Mainly used in Unit Tests.
 */
public class KotlinStreamLogger(
    private val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss''SSS", Locale.ENGLISH),
) : StreamLogger {

    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {
        val now = dateFormat.format(now())
        val thread = Thread.currentThread().run { "$name:$id" }
        val composed = "$now ($thread) [${priority.stringify()}/$tag]: $message"
        val finalMessage = throwable?.let {
            "$composed\n${it.stringify()}"
        } ?: composed
        when (priority) {
            ERROR, ASSERT -> System.err.println(finalMessage)
            else -> println(finalMessage)
        }
    }

    private fun now() = System.currentTimeMillis()
}
