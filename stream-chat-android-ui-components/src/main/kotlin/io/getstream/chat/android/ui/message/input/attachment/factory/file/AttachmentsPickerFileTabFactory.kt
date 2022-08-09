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

package io.getstream.chat.android.ui.message.input.attachment.factory.file

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.ui.message.input.attachment.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.message.input.attachment.factory.file.internal.FileAttachmentFragment

/**
 * A factory responsible for creating file attachments tab in the attachments picker.
 */
public class AttachmentsPickerFileTabFactory : AttachmentsPickerTabFactory {

    /**
     * Creates an icon for file attachments tab.
     *
     * @param style Style for [MessageInputView].
     * @return The Drawable used as the teb icon.
     */
    override fun createTabIcon(style: MessageInputViewStyle): Drawable {
        return style.attachmentSelectionDialogStyle.fileAttachmentIcon
    }

    /**
     * Provides a new Fragment associated with this file attachments tab.
     *
     * @param style Style for [MessageInputView].
     * @param attachmentsPickerTabListener The listener invoked when attachments are selected in the tab.
     * @return A new content Fragment for the tab.
     */
    override fun createTabFragment(
        style: MessageInputViewStyle,
        attachmentsPickerTabListener: AttachmentsPickerTabListener,
    ): Fragment {
        return FileAttachmentFragment.newInstance(style).apply {
            setAttachmentsPickerTabListener(attachmentsPickerTabListener)
        }
    }
}
