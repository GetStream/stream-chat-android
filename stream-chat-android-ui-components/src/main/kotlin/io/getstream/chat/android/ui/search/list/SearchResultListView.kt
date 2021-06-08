package io.getstream.chat.android.ui.search.list

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.getstream.sdk.chat.view.EndlessScrollListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiSearchResultListViewBinding
import io.getstream.chat.android.ui.search.internal.SearchResultListAdapter

public class SearchResultListView : ViewFlipper {

    private companion object {
        const val LOAD_MORE_THRESHOLD = 10
    }

    private object Flipper {
        const val RESULTS = 0
        const val EMPTY = 1
        const val LOADING = 2
    }

    private val binding = StreamUiSearchResultListViewBinding.inflate(streamThemeInflater, this)

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    private var loadMoreListener: LoadMoreListener? = null

    private val adapter = SearchResultListAdapter(context, ChatDomain.instance())

    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.onLoadMoreRequested()
    }

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)

        binding.searchListView.apply {
            setHasFixedSize(true)
            adapter = this@SearchResultListView.adapter

            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

            addOnScrollListener(scrollListener)
        }
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    public fun showMessages(query: String, messages: List<Message>) {
        val isEmpty = messages.isEmpty()

        displayedChild = if (isEmpty) Flipper.EMPTY else Flipper.RESULTS

        if (!isEmpty) {
            val count = messages.count()
            binding.searchInfoBar.text =
                resources.getQuantityString(R.plurals.stream_ui_search_results_count, count, count)
            scrollListener.enablePagination()
        } else {
            binding.emptyLabel.text = resources.getString(R.string.stream_ui_search_results_empty, query)
            scrollListener.disablePagination()
        }

        adapter.submitList(messages)
    }

    public fun showLoading() {
        displayedChild = Flipper.LOADING
        scrollListener.disablePagination()
    }

    public fun showError() {
        Toast.makeText(context, R.string.stream_ui_search_results_error, Toast.LENGTH_SHORT).show()
    }

    public fun setSearchResultSelectedListener(searchResultSelectedListener: SearchResultSelectedListener?) {
        adapter.setSearchResultSelectedListener(searchResultSelectedListener)
    }

    public fun setLoadMoreListener(loadMoreListener: LoadMoreListener?) {
        this.loadMoreListener = loadMoreListener
    }

    public fun interface SearchResultSelectedListener {
        public fun onSearchResultSelected(message: Message)
    }

    public fun interface LoadMoreListener {
        public fun onLoadMoreRequested()
    }
}
