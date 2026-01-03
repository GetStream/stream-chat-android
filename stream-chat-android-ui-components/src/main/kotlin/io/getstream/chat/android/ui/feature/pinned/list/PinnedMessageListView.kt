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

package io.getstream.chat.android.ui.feature.pinned.list

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.databinding.StreamUiPinnedMessageListViewBinding
import io.getstream.chat.android.ui.feature.pinned.list.internal.PinnedMessageListAdapter
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.EndlessScrollListener

public class PinnedMessageListView : ViewFlipper {

    private companion object {
        const val LOAD_MORE_THRESHOLD = 10
    }

    private object Flipper {
        const val RESULTS = 0
        const val EMPTY = 1
        const val LOADING = 2
    }

    private val binding = StreamUiPinnedMessageListViewBinding.inflate(streamThemeInflater, this)

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    private var loadMoreListener: LoadMoreListener? = null

    private val adapter = PinnedMessageListAdapter()

    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.onLoadMoreRequested()
    }

    private lateinit var style: PinnedMessageListViewStyle

    private fun init(attrs: AttributeSet?) {
        style = PinnedMessageListViewStyle(context, attrs).also { style ->
            setBackgroundColor(style.backgroundColor)
            binding.emptyImage.setImageDrawable(style.emptyStateDrawable)
            adapter.messagePreviewStyle = style.messagePreviewStyle
        }

        binding.pinnedMessageListRecyclerView.apply {
            setHasFixedSize(true)
            adapter = this@PinnedMessageListView.adapter
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

        binding.emptyDescriptionTextView.text = Html.fromHtml(
            context.getString(R.string.stream_ui_pinned_message_list_empty_description),
        )
    }

    public fun showMessages(messageResults: List<MessageResult>) {
        val isEmpty = messageResults.isEmpty()

        displayedChild = if (isEmpty) Flipper.EMPTY else Flipper.RESULTS

        adapter.submitList(messageResults)
        scrollListener.enablePagination()
    }

    public fun showLoading() {
        if (adapter.itemCount == 0) {
            displayedChild = Flipper.LOADING
        }
        scrollListener.disablePagination()
    }

    public fun showError() {
        Toast.makeText(context, R.string.stream_ui_pinned_message_list_results_error, Toast.LENGTH_SHORT).show()
    }

    public fun setPinnedMessageSelectedListener(pinnedMessageSelectedListener: PinnedMessageSelectedListener?) {
        adapter.setPinnedMessageSelectedListener(pinnedMessageSelectedListener)
    }

    public fun setLoadMoreListener(loadMoreListener: LoadMoreListener?) {
        this.loadMoreListener = loadMoreListener
    }

    public fun interface PinnedMessageSelectedListener {
        public fun onPinnedMessageSelected(message: Message)
    }

    public fun interface LoadMoreListener {
        public fun onLoadMoreRequested()
    }
}
