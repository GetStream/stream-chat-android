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

package io.getstream.chat.android.client.models

public sealed class MessageSyncContent

public sealed class MessageSyncInProgress : MessageSyncContent()
public sealed class MessageSyncFailed : MessageSyncContent()

public object MessageAwaitingAttachments : MessageSyncInProgress() {
    override fun toString(): String = "MessageAwaitingAttachments"
}

public data class MessageModerationFailed(
    val violations: List<Violation>,
) : MessageSyncFailed() {
    public data class Violation(
        val code: Int,
        val messages: List<String>,
    )
}
