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

package io.getstream.chat.android.ui.common.helper

/**
 * An interface that provides the current time in milliseconds.
 */
public fun interface TimeProvider : () -> Long {

    /**
     * Returns the current time in milliseconds.
     */
    override fun invoke(): Long

    public companion object {
        /**
         * The default implementation of [TimeProvider] that returns the current time in milliseconds.
         */
        @JvmStatic
        public val DEFAULT: TimeProvider = TimeProvider { System.currentTimeMillis() }
    }
}
