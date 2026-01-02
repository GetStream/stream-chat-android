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

package io.getstream.chat.android.ui.common.utils.typing

/**
 * Designed to buffer typing inputs.
 *
 * Its implementation should receive keystroke events by calling [TypingUpdatesBuffer.onKeystroke]
 * which it will internally buffer and send start and stop typing API calls accordingly.
 * This cuts down on unnecessary API calls.
 *
 * For the default implementation see [DefaultTypingUpdatesBuffer].
 */
public interface TypingUpdatesBuffer {

    /**
     * Should be called on every input change.
     *
     * @param [inputText] the current input text.
     */
    public fun onKeystroke(inputText: String)

    /**
     * Should send a stop typing event manually.
     *
     * Useful for runtime hygiene such as responding to lifecycle events.
     */
    public fun clear()
}
