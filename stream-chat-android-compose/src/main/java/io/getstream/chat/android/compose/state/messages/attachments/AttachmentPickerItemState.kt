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
 * @param selection The current selection state of the item. See [Selection] for possible states.
 */
public data class AttachmentPickerItemState(
    val attachmentMetaData: AttachmentMetaData,
    val selection: Selection = Selection.Unselected,
) {

    /**
     * Represents the selection state of an attachment picker item.
     */
    public sealed interface Selection {

        /**
         * Indicates that the attachment is selected.
         *
         * @param position The 1-based position of this attachment in the selection order.
         * This is displayed as a badge on the item to show the selection order when
         * multiple attachments are selected.
         */
        public data class Selected(val position: Int) : Selection

        /**
         * Indicates that the attachment is not selected.
         */
        public data object Unselected : Selection
    }

    /**
     * Convenience property to check if this item is currently selected.
     *
     * @return `true` if the item is selected, `false` otherwise.
     */
    public val isSelected: Boolean get() = selection is Selection.Selected
}
