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

package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.client.logger.ChatLoggerHandler

internal class TestLoggerHandler : ChatLoggerHandler {
    override fun logT(throwable: Throwable) {
        println("logT: $throwable")
    }

    override fun logT(tag: Any, throwable: Throwable) {
        println("logT: $throwable")
    }

    override fun logI(tag: Any, message: String) {
        println("logI: $tag $message")
    }

    override fun logD(tag: Any, message: String) {
        println("logD: $tag $message")
    }

    override fun logW(tag: Any, message: String) {
        println("logW: $tag $message")
    }

    override fun logE(tag: Any, message: String) {
        println("logE: $tag $message")
    }

    override fun logE(tag: Any, message: String, throwable: Throwable) {
        println("logE: $tag $message $throwable")
    }
}
