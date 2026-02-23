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

package io.getstream.chat.android.compose.state.messages.attachments

import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

/**
 * Represents the state of a single item in the attachment picker.
 *
 * Each item displayed in the attachment picker (images, videos, files) is represented by this class,
 * which holds both the metadata about the attachment and its current selection state.
 *
 * @param attachmentMetaData The metadata describing the attachment, including its URI, file name,
 * size, MIME type, and other relevant information.
 * @param isSelected Whether this item is currently selected.
 */
public data class AttachmentPickerItemState(
    val attachmentMetaData: AttachmentMetaData,
    val isSelected: Boolean = false,
)
