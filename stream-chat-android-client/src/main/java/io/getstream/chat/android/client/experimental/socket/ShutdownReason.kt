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

package io.getstream.chat.android.client.experimental.socket

/**
 * Used to initiate a shutdown of a WebSocket.
 *
 * @property code Status code as defined by [Section 7.4 of RFC 6455](http://tools.ietf.org/html/rfc6455#section-7.4)
 * or `0`.
 * @property reason Reason for shutting down.
 */
internal data class ShutdownReason(val code: Int, val reason: String) {
    companion object {
        private const val NORMAL_CLOSURE_STATUS_CODE = 1000
        private const val NORMAL_CLOSURE_REASON = "Normal closure"

        @JvmField
        val GRACEFUL = ShutdownReason(NORMAL_CLOSURE_STATUS_CODE, NORMAL_CLOSURE_REASON)
    }
}
