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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.internal

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener

/**
 * An adapter that handles the creation of tabs for the attachment picker.
 *
 * @param style The style for the attachment picker dialog.
 * @param attachmentsPickerTabFactories The list of factories for the tabs in the attachment picker.
 * @param attachmentsPickerTabListener The listener invoked when attachments are selected in the tab.
 */
internal class AttachmentDialogPagerAdapter(
    fragment: Fragment,
    private val style: AttachmentsPickerDialogStyle,
    private val attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory>,
    private val attachmentsPickerTabListener: AttachmentsPickerTabListener,
) : FragmentStateAdapter(fragment) {

    /**
     * Creates a new Fragment with the tab content.
     *
     * @param position The position of the page.
     * @return A new Fragment with the tab content.
     */
    override fun createFragment(position: Int): Fragment = if (position < attachmentsPickerTabFactories.count()) {
        attachmentsPickerTabFactories[position].createTabFragment(style, attachmentsPickerTabListener)
    } else {
        throw IllegalArgumentException("Can not create page for position $position")
    }

    /**
     * Returns the total number of tabs in the attachment picker.
     *
     * @return The total number of tabs in the attachment picker.
     */
    override fun getItemCount(): Int = attachmentsPickerTabFactories.count()
}
