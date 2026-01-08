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

package io.getstream.chat.android.ui.feature.messages.list.background

import android.content.Context
import android.graphics.drawable.Drawable
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem

/**
 * Drawer of background of message items.
 */
public interface MessageBackgroundFactory {

    /**
     * Background for message of plain text.
     */
    public fun plainTextMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * Background for deleted messages.
     */
    public fun deletedMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * Background for messages with attachments and text.
     */
    public fun textAndAttachmentMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * Background for messages containing file attachments.
     */
    public fun fileAttachmentsMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * Background for messages containing link attachments and no other types of attachments.
     */
    public fun linkAttachmentMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * ShapeAppearanceModel for giphy card.
     */
    public fun giphyAppearanceModel(context: Context): Drawable

    /**
     * Background for message with image attachments.
     */
    public fun imageAttachmentMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * Background for poll messages.
     */
    public fun pollMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable
}
