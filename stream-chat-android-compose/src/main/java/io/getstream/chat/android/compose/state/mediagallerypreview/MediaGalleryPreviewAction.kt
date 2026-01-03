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

package io.getstream.chat.android.compose.state.mediagallerypreview

import io.getstream.chat.android.models.Message

/**
 * Represents the actions the user can take with media attachments in the Media Gallery Preview
 * feature.
 *
 * @property message The message that the action is being performed on.
 */
public sealed class MediaGalleryPreviewAction {
    internal abstract val message: Message
}

/**
 * Should take the user back to the message list with a pre-packaged
 * quoted reply in the message input.
 */
public data class Reply(override val message: Message) : MediaGalleryPreviewAction()

/**
 * Should show the message containing the attachments in the message list.
 */
public data class ShowInChat(override val message: Message) : MediaGalleryPreviewAction()

/**
 * Should save the media to storage.
 */
public data class SaveMedia(override val message: Message) : MediaGalleryPreviewAction()

/**
 * Should remove the selected media attachment from the original message.
 */
public data class Delete(override val message: Message) : MediaGalleryPreviewAction()
