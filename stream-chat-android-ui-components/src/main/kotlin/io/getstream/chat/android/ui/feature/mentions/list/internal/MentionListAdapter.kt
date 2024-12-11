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

package io.getstream.chat.android.ui.feature.mentions.list.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionListBinding
import io.getstream.chat.android.ui.feature.internal.MessageResultDiffCallback
import io.getstream.chat.android.ui.feature.mentions.list.MentionListView.MentionSelectedListener
import io.getstream.chat.android.ui.feature.mentions.list.internal.MentionListAdapter.MessagePreviewViewHolder
import io.getstream.chat.android.ui.feature.messages.preview.MessagePreviewStyle
import io.getstream.chat.android.ui.feature.messages.preview.internal.MessagePreviewView
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class MentionListAdapter : ListAdapter<MessageResult, MessagePreviewViewHolder>(MessageResultDiffCallback) {

    private var mentionSelectedListener: MentionSelectedListener? = null

    var previewStyle: MessagePreviewStyle? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePreviewViewHolder {
        return StreamUiItemMentionListBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { binding ->
                previewStyle?.let(binding.root::styleView)
                MessagePreviewViewHolder(binding.root)
            }
    }

    override fun onBindViewHolder(holder: MessagePreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setMentionSelectedListener(mentionSelectedListener: MentionSelectedListener?) {
        this.mentionSelectedListener = mentionSelectedListener
    }

    inner class MessagePreviewViewHolder(
        private val view: MessagePreviewView,
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var message: Message

        init {
            view.setOnClickListener {
                mentionSelectedListener?.onMentionSelected(message)
            }
        }

        internal fun bind(messageResult: MessageResult) {
            this.message = messageResult.message
            view.renderMessageResult(messageResult)
        }
    }
}
