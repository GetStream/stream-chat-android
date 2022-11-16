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

package io.getstream.chat.android.guides

import android.content.Context
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager
import io.getstream.chat.android.ui.helper.SupportedReactions

/**
 * Reset [ChatUI] fields to the default values so that customizations showcased in one
 * guide don't affect other guides.
 *
 * @param context The Application [Context].
 */
fun ChatUI.cleanup(context: Context) {
    attachmentFactoryManager = AttachmentFactoryManager()
    attachmentPreviewFactoryManager = AttachmentPreviewFactoryManager()
    quotedAttachmentFactoryManager = QuotedAttachmentFactoryManager()
    supportedReactions = SupportedReactions(context)
}
