/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.system

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import io.getstream.chat.android.ui.common.permissions.VisualMediaType
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.system.internal.AttachmentsPickerSystemConfig
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.system.internal.AttachmentsPickerSystemFragment

/**
 * An attachment factory that creates a tab with the few icons and uses system pickers instead.
 *
 * @param mediaAttachmentsTabEnabled If the option to pick media (images/videos) is included in the attachments picker.
 * @param visualMediaAllowMultiple If true, multiple visual media items can be selected.
 * @param visualMediaType The types of visual media that can be picked.
 * @param fileAttachmentsTabEnabled If the option to pick files is included in the attachments picker.
 * @param cameraAttachmentsTabEnabled If the option to capture an image/video is included in the attachments picker.
 * @param pollAttachmentsTabEnabled If the option to create a poll is included in the attachments picker.
 */
public class AttachmentsPickerSystemTabFactory(
    private val mediaAttachmentsTabEnabled: Boolean,
    private val visualMediaAllowMultiple: Boolean,
    private val visualMediaType: VisualMediaType,
    private val fileAttachmentsTabEnabled: Boolean,
    private val cameraAttachmentsTabEnabled: Boolean,
    private val pollAttachmentsTabEnabled: Boolean,
) : AttachmentsPickerTabFactory {

    /**
     * Create the tab icon.
     * @param style The style of the dialog.
     */
    override fun createTabIcon(style: AttachmentsPickerDialogStyle): Drawable = style.submitAttachmentsButtonIconDrawable

    /**
     * Create the tab fragment.
     * @param style The style of the dialog.
     * @param attachmentsPickerTabListener The listener for the tab.
     */
    override fun createTabFragment(
        style: AttachmentsPickerDialogStyle,
        attachmentsPickerTabListener: AttachmentsPickerTabListener,
    ): Fragment = AttachmentsPickerSystemFragment.newInstance(
        style,
        attachmentsPickerTabListener,
        AttachmentsPickerSystemConfig(
            visualMediaAttachmentsTabEnabled = mediaAttachmentsTabEnabled,
            visualMediaAllowMultiple = visualMediaAllowMultiple,
            visualMediaType = visualMediaType,
            fileAttachmentsTabEnabled = fileAttachmentsTabEnabled,
            cameraAttachmentsTabEnabled = cameraAttachmentsTabEnabled,
            pollAttachmentsTabEnabled = pollAttachmentsTabEnabled,
        ),
    )
}
