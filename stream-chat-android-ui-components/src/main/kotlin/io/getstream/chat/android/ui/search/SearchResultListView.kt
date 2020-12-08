package io.getstream.chat.android.ui.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.getstream.sdk.chat.view.EndlessScrollListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiSearchResultListViewBinding

public class SearchResultListView : LinearLayout {

    private companion object {
        const val LOAD_MORE_THRESHOLD = 10
    }

    private val binding = StreamUiSearchResultListViewBinding.inflate(LayoutInflater.from(context), this)

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private var loadMoreListener: LoadMoreListener? = null

    private val adapter = SearchResultListAdapter(context)

    private val scrollListener = EndlessScrollListener {
        loadMoreListener?.onLoadMoreRequested()
    }.apply {
        loadMoreThreshold = LOAD_MORE_THRESHOLD
    }

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)

        orientation = VERTICAL

        binding.searchListView.apply {
            setHasFixedSize(true)
            adapter = this@SearchResultListView.adapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
            addOnScrollListener(scrollListener)
        }
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    public fun setMessages(messages: List<Message>) {
        val count = messages.count()
        binding.searchInfoBar.text = when (count) {
            0 -> resources.getString(R.string.stream_ui_search_result_list_result_count_empty)
            else -> resources.getQuantityString(R.plurals.stream_ui_search_result_list_result_count, count, count)
        }
        adapter.submitList(messages)
    }

    public fun showLoading(isLoading: Boolean) {
        // TODO display loading
        scrollListener.paginationEnabled = !isLoading
    }

    public fun showError(isError: Boolean) {
        // TODO display errors
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
