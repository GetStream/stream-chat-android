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

package io.getstream.chat.android.ui.gallery.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class AttachmentGalleryPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val imageList: List<String>,
    private val imageClickListener: () -> Unit,
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        return AttachmentGalleryPageFragment.create(getItem(position), imageClickListener)
    }

    fun getItem(position: Int): String = imageList[position]
}
