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

package io.getstream.chat.android.client.logger

import io.getstream.chat.android.client.errors.ChatError

internal class TaggedLoggerImpl(
    private val tag: Any,
    private val logger: ChatLogger
) : TaggedLogger {

    override fun logI(message: String) {
        logger.logI(tag, message)
    }

    override fun logD(message: String) {
        logger.logD(tag, message)
    }

    override fun logW(message: String) {
        logger.logW(tag, message)
    }

    override fun logE(message: String) {
        logger.logE(tag, message)
    }

    override fun logE(throwable: Throwable) {
        logger.logE(tag, throwable)
    }

    override fun logE(message: String, throwable: Throwable) {
        logger.logE(tag, message, throwable)
    }

    override fun getLevel(): ChatLogLevel {
        return logger.getLevel()
    }

    override fun logE(message: String, chatError: ChatError) {
        logger.logE(tag, message, chatError)
    }

    override fun logE(chatError: ChatError) {
        logger.logE(tag, chatError)
    }
}
