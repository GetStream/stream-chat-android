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
 * Represents each attachment item in our attachment picker. Each item can be selected and has an
 * appropriate set of metadata to describe it.
 *
 * @param attachmentMetaData The metadata for the item, holding the links, size, types, name etc.
 * @param isSelected If the item is selected or not.
 */
public data class AttachmentPickerItemState(
    val attachmentMetaData: AttachmentMetaData,
    val isSelected: Boolean,
)
