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
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListenersAdapter

/**
 * Represents a handler that can handle custom attachments of certain type and create
 * ViewHolders for them.
 */
public interface AttachmentFactory {

    /**
     * Checks if this [AttachmentFactory] can consume attachments from the given message.
     *
     * @param message The message containing custom attachments that we are going to render.
     * @return True if the factory can handle the attachments from this message.
     */
    public fun canHandle(message: Message): Boolean

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
    ): InnerAttachmentViewHolder = InnerAttachmentViewHolder.stub(parent)

    /**
     * Create a ViewHolder for the custom attachments View which is aware of the parent's
     * ViewHolder lifecycle.
     *
     * @param message The message containing custom attachments that we are going to render.
     * @param listeners [MessageListListeners] with listeners for the message list.
     * @param parent The parent View where the attachment content view is supposed to be placed.
     * @return An inner ViewHolder with the attachment content view.
     */
    public fun createViewHolder(
        message: Message,
        listeners: MessageListListeners?,
        parent: ViewGroup,
    ): InnerAttachmentViewHolder {
        val adapter = listeners?.let { MessageListListenersAdapter(it) }
        return createViewHolder(message, adapter, parent)
    }
}
