/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Abstraction over the [ClipboardManager] that allows users to copy text to the clipboard.
 */
@ExperimentalStreamChatApi
public interface CopyToClipboardHandler {
    /**
     * Copies the given [text] to the clipboard.
     */
    public fun copy(text: String)

    public companion object {
        /**
         * Creates a new instance of [CopyToClipboardHandler] using the provided [context].
         */
        public operator fun invoke(context: Context): CopyToClipboardHandler =
            CopyToClipboardHandlerImpl(context = context.applicationContext)
    }
}

@OptIn(ExperimentalStreamChatApi::class)
internal class CopyToClipboardHandlerImpl(
    private val clipboardManager: ClipboardManager,
) : CopyToClipboardHandler {

    constructor(context: Context) : this(
        clipboardManager = requireNotNull(context.getSystemService<ClipboardManager>()),
    )

    override fun copy(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(PLAIN_TEXT_LABEL, text))
    }
}

private const val PLAIN_TEXT_LABEL = "plain text"
