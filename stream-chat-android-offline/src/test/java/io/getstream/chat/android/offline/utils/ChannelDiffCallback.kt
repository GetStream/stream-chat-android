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

package io.getstream.chat.android.offline.utils

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.models.Channel

internal data class ChannelDiffCallback(
    var oldChannels: List<Channel>,
    var newChannels: List<Channel>,
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldChannels.size

    override fun getNewListSize(): Int = newChannels.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldChannels[oldItemPosition].id == newChannels[newItemPosition].id

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int,
    ): Boolean = oldChannels[oldItemPosition] == newChannels[newItemPosition]
}
