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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.poll

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiPollOptionBinding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

public class OptionsAdapter(
    private val onOptionChange: (id: Int, text: String) -> Unit,
) : ListAdapter<PollAnswer, OptionsAdapter.OptionViewHolder>(OptionDiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder =
        OptionViewHolder(
            parent = parent,
            onOptionChange = onOptionChange,
        )

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    public class OptionViewHolder(
        parent: ViewGroup,
        private val binding: StreamUiPollOptionBinding = StreamUiPollOptionBinding.inflate(
            parent.streamThemeInflater,
            parent,
            false,
        ),
        private val onOptionChange: (id: Int, text: String) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var pollAnswer: PollAnswer

        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* no-op */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* no-op */ }
            override fun afterTextChanged(s: Editable?) {
                pollAnswer.updateErrorState()
                onOptionChange(pollAnswer.id, s.toString())
            }
        }

        public fun bind(pollAnswer: PollAnswer) {
            this.pollAnswer = pollAnswer
            binding.option.removeTextChangedListener(textWatcher)
            binding.option.setText(pollAnswer.text)
            binding.option.setSelection(pollAnswer.text.length)
            pollAnswer.updateErrorState()
            binding.option.addTextChangedListener(textWatcher)
        }

        private fun PollAnswer.updateErrorState() {
            binding.option.error = when {
                duplicateError ->
                    binding.root.context.getString(R.string.stream_ui_poll_this_is_already_an_option)
                else -> null
            }
        }
    }

    private object OptionDiffCallback : DiffUtil.ItemCallback<PollAnswer>() {
        override fun areItemsTheSame(oldItem: PollAnswer, newItem: PollAnswer): Boolean = (oldItem.id == newItem.id)
        override fun areContentsTheSame(oldItem: PollAnswer, newItem: PollAnswer): Boolean =
            (oldItem.duplicateError == newItem.duplicateError)
    }
}
