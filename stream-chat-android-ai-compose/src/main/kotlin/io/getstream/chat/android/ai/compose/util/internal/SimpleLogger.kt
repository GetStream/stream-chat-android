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

package io.getstream.chat.android.ai.compose.util.internal

import android.util.Log

/**
 * Simple logger interface API.
 */
@Suppress("FunctionMinLength")
internal interface SimpleLogger {
    fun d(message: () -> String)
    fun v(message: () -> String)
    fun w(message: () -> String)
    fun e(message: () -> String)
    fun e(throwable: Throwable, message: () -> String)
}

/**
 * Simple logger implementation using Android's Log class.
 */
internal class AndroidSimpleLogger(private val tag: String) : SimpleLogger {
    override fun d(message: () -> String) {
        Log.d(tag, message())
    }

    override fun v(message: () -> String) {
        Log.v(tag, message())
    }

    override fun w(message: () -> String) {
        Log.w(tag, message())
    }

    override fun e(message: () -> String) {
        Log.e(tag, message())
    }

    override fun e(throwable: Throwable, message: () -> String) {
        Log.e(tag, message(), throwable)
    }
}

/**
 * Creates a simple logger with the given tag.
 */
internal fun simpleLogger(tag: String): SimpleLogger = AndroidSimpleLogger(tag)
