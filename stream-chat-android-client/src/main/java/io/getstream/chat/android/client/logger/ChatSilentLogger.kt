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

internal class ChatSilentLogger : ChatLogger {

    override fun getLevel(): ChatLogLevel {
        return ChatLogLevel.NOTHING
    }

    override fun logE(tag: Any, throwable: Throwable) {
        // silent
    }

    override fun logE(tag: Any, message: String, throwable: Throwable) {
        // silent
    }

    override fun logE(tag: Any, chatError: ChatError) {
        // silent
    }

    override fun logE(tag: Any, message: String, chatError: ChatError) {
        // silent
    }

    override fun logI(tag: Any, message: String) {
        // silent
    }

    override fun logD(tag: Any, message: String) {
        // silent
    }

    override fun logV(tag: Any, message: String) {
        // silent
    }

    override fun logW(tag: Any, message: String) {
        // silent
    }

    override fun logE(tag: Any, message: String) {
        // silent
    }
}
