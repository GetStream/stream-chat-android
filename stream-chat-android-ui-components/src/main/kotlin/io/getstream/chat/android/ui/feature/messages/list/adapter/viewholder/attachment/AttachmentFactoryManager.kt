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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners

/**
 * A manager for registered custom attachment factories.
 */
public class AttachmentFactoryManager(
    private val attachmentFactories: List<AttachmentFactory> = listOf(
        UnsupportedAttachmentFactory(),
    ),
) {
    /**
     * Checks if any [AttachmentFactory] can consume attachments from the given message.
     *
     * @param message The message containing custom attachments that we are going to render.
     * @return True if there is a factory that can handle the attachments from this message.
     */
    public fun canHandle(message: Message): Boolean {
        return attachmentFactories.any { it.canHandle(message) }
    }

    /**
     * Create a ViewHolder for the custom attachments View which is aware of the parent's
     * ViewHolder lifecycle.
     *
     * @param message The message containing custom attachments that we are going to render.
     * @param listeners [MessageListListenerContainer] with listeners for the message list.
     * @param parent The parent View where the attachment content view is supposed to be placed.
     * @return An inner ViewHolder with the attachment content view.
     */
    @Deprecated(
        message = "Use createViewHolder(message: Message, listeners: MessageListListeners?, parent: ViewGroup) instead",
        replaceWith = ReplaceWith("createViewHolder(message, listeners, parent)"),
        level = DeprecationLevel.WARNING,
    )
    public fun createViewHolder(
        message: Message,
        listeners: MessageListListenerContainer?,
        parent: ViewGroup,
    ): InnerAttachmentViewHolder {
        val factory = attachmentFactories.first { it.canHandle(message) }
        return factory.createViewHolder(message, listeners, parent)
    }

    public fun createViewHolder(
        message: Message,
        listeners: MessageListListeners?,
        parent: ViewGroup,
    ): InnerAttachmentViewHolder {
        val factory = attachmentFactories.first { it.canHandle(message) }
        return factory.createViewHolder(message, listeners, parent)
    }
}
