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

package io.getstream.chat.android.compose.state.messages.attachments

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult

/**
 * Represents the state of Attachment items, used to render and add handlers required for the attachment to work.
 *
 * @param message Data that represents the message information.
 * @param onLongItemClick Handler for a long click on the message item.
 * @param onImagePreviewResult Handler when the user selects an action to scroll to and focus an image.
 */
// TODO update documentation
public data class AttachmentState(
    val message: Message,
    val onLongItemClick: (Message) -> Unit = {},
    val onImagePreviewResult: (ImagePreviewResult?) -> Unit = {},
    val onAttachmentClick: ((onAttachmentClickState: OnAttachmentClickState) -> Unit)? = null,
)
