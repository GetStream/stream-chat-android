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

package io.getstream.chat.android.ui.feature.gallery.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType

internal class AttachmentGalleryPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val mediaList: List<Attachment>,
    private val mediaClickListener: () -> Unit,
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = mediaList.size

    override fun createFragment(position: Int): Fragment {
        val attachment = getItem(position)

        return when (attachment.type) {
            AttachmentType.IMAGE -> AttachmentGalleryImagePageFragment.create(attachment, mediaClickListener)
            AttachmentType.VIDEO -> AttachmentGalleryVideoPageFragment.create(attachment, mediaClickListener)
            else -> throw Throwable("Unsupported attachment type")
        }
    }

    fun getItem(position: Int): Attachment = mediaList[position]
}
