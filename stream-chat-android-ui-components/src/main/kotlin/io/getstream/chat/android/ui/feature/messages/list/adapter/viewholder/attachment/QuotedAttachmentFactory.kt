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

import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.models.Message

/**
 * Represents a handler that can handle quoted attachments of certain type and create views for them.
 */
public interface QuotedAttachmentFactory {

    /**
     * Checks if this [QuotedAttachmentFactory] can consume quoted attachments from the given message.
     *
     * @param message The message containing custom quoted attachments that we are going to render.
     * @return True if the factory can handle the  quoted attachments from this quoted message.
     */
    public fun canHandle(message: Message): Boolean

    /**
     * Create a view for the quoted attachments.
     *
     * @param message The message containing attachments that we are going to render.
     * @param parent The parent View where the attachment content view is supposed to be placed.
     * @return A quoted attachment view to be placed inside the quoted message.
     */
    public fun generateQuotedAttachmentView(
        message: Message,
        parent: ViewGroup,
    ): View
}
