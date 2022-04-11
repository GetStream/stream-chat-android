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

package io.getstream.logging.helper

import io.getstream.logging.Priority
import io.getstream.logging.StreamLogger
import java.io.PrintWriter
import java.io.StringWriter

private const val INITIAL_BUFFER_SIZE = 256

public fun Thread.stringify(): String {
    return "$name:$id"
}

public fun Priority.stringify(): String = when (this) {
    StreamLogger.VERBOSE -> "V"
    StreamLogger.DEBUG -> "D"
    StreamLogger.INFO -> "I"
    StreamLogger.WARN -> "W"
    StreamLogger.ERROR -> "E"
    StreamLogger.ASSERT -> "E"
    else -> "?"
}

public fun Throwable.stringify(): String {
    // Don't replace this with Log.getStackTraceString() - it hides
    // UnknownHostException, which is not what we want.
    val sw = StringWriter(INITIAL_BUFFER_SIZE)
    val pw = PrintWriter(sw, false)
    printStackTrace(pw)
    pw.flush()
    return sw.toString()
}
