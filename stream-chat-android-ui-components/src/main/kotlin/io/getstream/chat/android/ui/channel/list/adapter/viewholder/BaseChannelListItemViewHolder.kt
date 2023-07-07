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

package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff

public abstract class BaseChannelListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    @Deprecated(
        message = "This method is deprecated, and will be removed on V6",
        replaceWith = ReplaceWith(
            expression = "this.bind(channelItem, diff)",
            imports = arrayOf("io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem")
        )
    )
    public open fun bind(channel: Channel, diff: ChannelListPayloadDiff) { }
    public open fun bind(channelItem: ChannelListItem.ChannelItem, diff: ChannelListPayloadDiff) {
        bind(channelItem.channel, diff)
    }
}
