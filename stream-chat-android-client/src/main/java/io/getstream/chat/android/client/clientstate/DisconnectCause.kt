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

package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.errors.ChatNetworkError

/**
 * Sealed class represents possible cause of disconnection.
 */
public sealed class DisconnectCause {

    /**
     * Happens when networks is not available anymore.
     */
    public object NetworkNotAvailable : DisconnectCause()

    /**
     * Happens when some non critical error occurs.
     * @param error Instance of [ChatNetworkError] as a reason of it.
     */
    public class Error(public val error: ChatNetworkError?) : DisconnectCause()

    /**
     * Happens when a critical error occurs. Connection can't be restored after such disconnection.
     * @param error Instance of [ChatNetworkError] as a reason of it.
     */
    public class UnrecoverableError(public val error: ChatNetworkError?) : DisconnectCause()

    /**
     * Happens when disconnection has been done intentionally. E.g. we release connection when app went to background.
     */
    public object ConnectionReleased : DisconnectCause()
}
