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

import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.DefaultQuotedAttachmentView

/**
 * Factory for attachments that the SDK falls back when all the other [QuotedAttachmentFactory] don't support the
 * attachment type. It simply shows a file icon with the attachment title.
 */
public class FallbackQuotedAttachmentMessageFactory : QuotedAttachmentFactory {

    /**
     * @param message The quoted message with the attachments we wish to render.
     *
     * @return If the factory can handle the given quoted message attachment or not.
     */
    override fun canHandle(message: Message): Boolean = true

    /**
     * Generates a [DefaultQuotedAttachmentView] to render the attachment.
     *
     * @param message The quoted message holding the attachments.
     * @param parent The parent [ViewGroup] in which the attachment will be rendered.
     *
     * @return [DefaultQuotedAttachmentView] that will be rendered inside the quoted message.
     */
    override fun generateQuotedAttachmentView(message: Message, parent: ViewGroup): View = DefaultQuotedAttachmentView(parent.context).apply {
        setImageResource(R.drawable.stream_ui_ic_file)
    }
}
