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
 
package io.getstream.chat.android.ui.message.input.attachment.internal

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.camera.internal.CameraAttachmentFragment
import io.getstream.chat.android.ui.message.input.attachment.file.internal.FileAttachmentFragment
import io.getstream.chat.android.ui.message.input.attachment.media.internal.MediaAttachmentFragment

internal class AttachmentDialogPagerAdapter(fragment: Fragment, private val style: MessageInputViewStyle) :
    FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_MEDIA_ATTACHMENT -> MediaAttachmentFragment.newInstance(style)
            PAGE_FILE_ATTACHMENT -> FileAttachmentFragment.newInstance(style)
            PAGE_CAMERA_ATTACHMENT -> CameraAttachmentFragment.newInstance(style.attachmentSelectionDialogStyle)
            else -> throw IllegalArgumentException("Can not create page for position $position")
        }
    }

    override fun getItemCount(): Int = PAGE_COUNT

    companion object {
        const val PAGE_MEDIA_ATTACHMENT: Int = 0
        const val PAGE_FILE_ATTACHMENT: Int = 1
        const val PAGE_CAMERA_ATTACHMENT: Int = 2
        const val PAGE_COUNT: Int = 3
    }
}
