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

public interface TaggedLogger {
    public fun logI(message: String)

    public fun logD(message: String)

    public fun logV(message: String)

    public fun logW(message: String)

    public fun logE(message: String)

    public fun logE(throwable: Throwable)

    public fun logE(chatError: ChatError)

    public fun logE(message: String, throwable: Throwable)

    public fun logE(message: String, chatError: ChatError)

    public fun getLevel(): ChatLogLevel
}
