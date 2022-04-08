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

package io.getstream.logging

internal class TaggedLoggerImpl(
    private val tag: String,
    private val delegate: StreamLogger,
) : TaggedLogger {

    override fun e(throwable: Throwable, message: () -> String) {
        delegate.log(StreamLogger.ERROR, tag, message, throwable)
    }

    override fun e(message: () -> String) {
        delegate.log(StreamLogger.ERROR, tag, message)
    }

    override fun w(message: () -> String) {
        delegate.log(StreamLogger.WARN, tag, message)
    }

    override fun i(message: () -> String) {
        delegate.log(StreamLogger.INFO, tag, message)
    }

    override fun d(message: () -> String) {
        delegate.log(StreamLogger.DEBUG, tag, message)
    }

    override fun v(message: () -> String) {
        delegate.log(StreamLogger.VERBOSE, tag, message)
    }
}
