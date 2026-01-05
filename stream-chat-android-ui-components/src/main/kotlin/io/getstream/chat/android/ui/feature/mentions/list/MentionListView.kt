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

package io.getstream.chat.android.ui.feature.mentions.list

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.databinding.StreamUiMentionListViewBinding
import io.getstream.chat.android.ui.feature.mentions.list.internal.MentionListAdapter
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.EndlessScrollListener

/**
 * View used to display messages that contain a mention.
 */
public class MentionListView : ViewFlipper {

    private companion object {
        private const val LOAD_MORE_THRESHOLD = 10
    }

    private object Flipper {
        const val RESULTS = 0
        const val EMPTY = 1
        const val LOADING = 2
    }

    private val binding = StreamUiMentionListViewBinding.inflate(streamThemeInflater, this)

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    private val adapter = MentionListAdapter()

    private var loadMoreListener: LoadMoreListener? = null

    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.onLoadMoreRequested()
    }

    private lateinit var style: MentionListViewStyle

    private fun init(attrs: AttributeSet?) {
        style = MentionListViewStyle(context, attrs).also { style ->
            setBackgroundColor(style.backgroundColor)
            binding.emptyImage.setImageDrawable(style.emptyStateDrawable)
            adapter.previewStyle = style.messagePreviewStyle
        }

        binding.mentionListRecyclerView.apply {
            setHasFixedSize(true)
            adapter = this@MentionListView.adapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL,
                ).apply {
                    setDrawable(AppCompatResources.getDrawable(context, R.drawable.stream_ui_divider)!!)
                },
            )
            addOnScrollListener(scrollListener)
        }
    }

    /**
     * Shows the list of messages that contain the mention.
     *
     * @param messages The list of messages that contain the mention.
     * @param isLoadingMore If there are more messages loading.
     */
    public fun showMessages(messages: List<MessageResult>, isLoadingMore: Boolean = false) {
        val isEmpty = messages.isEmpty()

        displayedChild = if (isEmpty) Flipper.EMPTY else Flipper.RESULTS

        val items = messages.map(MentionListItem::MessageItem) +
            if (isLoadingMore) listOf(MentionListItem.LoadingItem) else emptyList()
        adapter.submitList(items)
        scrollListener.enablePagination()
    }

    /**
     * Shows the loading UI.
     */
    public fun showLoading() {
        displayedChild = Flipper.LOADING
        scrollListener.disablePagination()
    }

    /**
     * Shows a generic error message.
     */
    public fun showError() {
        Toast.makeText(context, R.string.stream_ui_mention_list_error, Toast.LENGTH_SHORT).show()
    }

    /**
     * Sets the listener for when a mention is selected.
     */
    public fun setMentionSelectedListener(mentionSelectedListener: MentionSelectedListener?) {
        adapter.mentionSelectedListener = mentionSelectedListener
    }

    /**
     * Sets the listener for when more messages should be loaded.
     */
    public fun setLoadMoreListener(loadMoreListener: LoadMoreListener?) {
        this.loadMoreListener = loadMoreListener
    }

    /**
     * Listener for when a mention is selected.
     */
    public fun interface MentionSelectedListener {
        /**
         * Called when a mention is selected.
         *
         * @param message The message that was selected.
         */
        public fun onMentionSelected(message: Message)
    }

    /**
     * Listener for when the end of the list is reached and more messages should be loaded.
     */
    public fun interface LoadMoreListener {
        /**
         * Called when more messages should be loaded.
         */
        public fun onLoadMoreRequested()
    }
}
