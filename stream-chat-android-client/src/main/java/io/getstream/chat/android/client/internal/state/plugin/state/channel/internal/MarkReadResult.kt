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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

/**
 * Outcome of a request to mark a channel as read.
 */
internal sealed interface MarkReadResult {

    /** The channel needs to be marked as read with a remote request. */
    data object RemoteRequired : MarkReadResult

    /** The channel was marked as read on-device; no remote request is needed. */
    data object HandledLocally : MarkReadResult

    /** The channel does not need to be marked as read. */
    data object NotNeeded : MarkReadResult
}
