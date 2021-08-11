package io.getstream.chat.android.ui.pinned.list

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.getstream.sdk.chat.view.EndlessScrollListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiPinnedMessageListViewBinding
import io.getstream.chat.android.ui.mention.list.MentionListViewStyle
import io.getstream.chat.android.ui.pinned.list.internal.PinnedMessageListAdapter

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

    private val adapter = PinnedMessageListAdapter(context, ChatDomain.instance())

    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.onLoadMoreRequested()
    }

    private lateinit var style: MentionListViewStyle

    private fun init(attrs: AttributeSet?) {
        style = MentionListViewStyle(context, attrs).also { style ->
            setBackgroundColor(context.getColorCompat(R.color.stream_ui_white_snow))
            binding.emptyImage.setImageDrawable(context.getDrawableCompat(R.drawable.stream_ui_ic_pinned_messages_empty))
            adapter.messagePreviewStyle = style.messagePreviewStyle
        }

        binding.pinnedMessageListRecyclerView.apply {
            setHasFixedSize(true)
            adapter = this@PinnedMessageListView.adapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                ).apply {
                    setDrawable(AppCompatResources.getDrawable(context, R.drawable.stream_ui_divider)!!)
                }
            )
            addOnScrollListener(scrollListener)
        }

        binding.emptyDescriptionTextView.text = Html.fromHtml(
            context.getString(R.string.stream_ui_pinned_message_list_empty_description)
        )
    }

    public fun showMessages(messages: List<Message>) {
        val isEmpty = messages.isEmpty()

        displayedChild = if (isEmpty) Flipper.EMPTY else Flipper.RESULTS

        adapter.submitList(messages)
    }

    public fun showLoading() {
        displayedChild = Flipper.LOADING
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
