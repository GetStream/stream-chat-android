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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.ChatUI

/**
 * A manager for registered quoted attachment factories.
 */
public class QuotedAttachmentFactoryManager(
    private val quotedAttachmentFactories: List<QuotedAttachmentFactory> = listOf(),
) {
    /**
     * Checks if any [QuotedAttachmentFactory] can consume attachments from the given message. If there are no
     * quoted message factories that can handle the attachment will default to the [AttachmentFactory]es that can.
     *
     * @param message The quoted message containing attachments that we are going to render.
     * @return True if there is a factory that can handle the attachments from this quoted message.
     */
    public fun canHandle(message: Message): Boolean = quotedAttachmentFactories.any { it.canHandle(message) } ||
        ChatUI.attachmentFactoryManager.canHandle(message)

    /**
     * Create and add a view for the quoted attachments.
     *
     * @param message The message containing attachments that we are going to render.
     * @param parent The parent View where the attachment content view is supposed to be placed.
     */
    public fun createAndAddQuotedView(
        message: Message,
        parent: ViewGroup,
    ) {
        val quotedAttachmentFactory =
            quotedAttachmentFactories.firstOrNull { it.canHandle(message) } ?: FallbackQuotedAttachmentMessageFactory()
        val view = quotedAttachmentFactory.generateQuotedAttachmentView(message, parent)

        parent.removeAllViews()
        parent.addView(view)
    }
}
