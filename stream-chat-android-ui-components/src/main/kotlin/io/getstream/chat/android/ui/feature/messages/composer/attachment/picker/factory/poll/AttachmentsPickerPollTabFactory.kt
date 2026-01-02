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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.poll

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.poll.internal.PollAttachmentFragment

/**
 * A factory responsible for creating poll attachments tab in the attachments picker.
 */
public class AttachmentsPickerPollTabFactory : AttachmentsPickerTabFactory {

    /**
     * Creates an icon for poll attachments tab.
     *
     * @param style The style for the attachment picker dialog.
     * @return The Drawable used as the teb icon.
     */
    override fun createTabIcon(style: AttachmentsPickerDialogStyle): Drawable {
        return style.pollAttachmentsTabIconDrawable
    }

    /**
     * Provides a new Fragment associated with this camera attachments tab.
     *
     * @param style The style for the attachment picker dialog.
     * @param attachmentsPickerTabListener The listener invoked when attachments are selected in the tab.
     * @return A new content Fragment for the tab.
     */
    override fun createTabFragment(
        style: AttachmentsPickerDialogStyle,
        attachmentsPickerTabListener: AttachmentsPickerTabListener,
    ): Fragment {
        return PollAttachmentFragment.newInstance().apply {
            setAttachmentsPickerTabListener(attachmentsPickerTabListener)
        }
    }
}
