/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.list

import androidx.compose.runtime.Stable

/**
 * Represents where the thread messages start.
 */
@Stable
public sealed class ThreadMessagesStart {

    /**
     * Thread messages start from the top of the component.
     */
    @Stable public object TOP : ThreadMessagesStart() {
        override fun toString(): String = "TOP"
    }

    /**
     * Thread messages start from the bottom of the component.
     */
    @Stable public object BOTTOM : ThreadMessagesStart() {
        override fun toString(): String = "BOTTOM"
    }
}
