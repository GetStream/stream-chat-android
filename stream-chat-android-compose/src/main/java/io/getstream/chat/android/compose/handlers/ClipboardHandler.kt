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

package io.getstream.chat.android.compose.handlers

import android.content.ClipData
import android.content.ClipboardManager
import io.getstream.chat.android.models.Message

/**
 * Abstraction over the [ClipboardHandlerImpl] that allows users to copy messages.
 */
public fun interface ClipboardHandler {

    /**
     * @param message The message to copy.
     */
    public fun copyMessage(message: Message)
}

/**
 * A simple implementation that relies on the [clipboardManager] to copy messages.
 *
 * @param clipboardManager System service that allows for clipboard operations, such as putting
 * new data on the clipboard.
 */
public class ClipboardHandlerImpl(private val clipboardManager: ClipboardManager) : ClipboardHandler {

    /**
     * Allows users to copy the message text.
     *
     * @param message Message to copy the text from.
     */
    override fun copyMessage(message: Message) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("message", message.text))
    }
}
