package io.getstream.chat.android.ui.mention.list

import android.content.Context
import android.graphics.Typeface
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
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMentionListViewBinding
import io.getstream.chat.android.ui.mention.list.internal.MentionListAdapter
import io.getstream.chat.android.ui.message.preview.internal.MessagePreviewView

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

    private val adapter = MentionListAdapter(context, ChatDomain.instance())

    private var loadMoreListener: LoadMoreListener? = null

    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        loadMoreListener?.onLoadMoreRequested()
    }

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)

        context.obtainStyledAttributes(
            attrs,
            R.styleable.MentionListView,
            R.attr.streamUiMentionListStyle,
            R.style.StreamUi_MentionList
        ).use { typedArray ->
            typedArray.getColor(
                R.styleable.MentionListView_streamUiBackground,
                context.getColorCompat(R.color.stream_ui_white_snow)
            ).let(::setBackgroundColor)

            typedArray.getDrawable(
                R.styleable.MentionListView_streamUiEmptyStateDrawable
            ).let(binding.emptyImage::setImageDrawable)

            val senderTextStyle = TextStyle.Builder(typedArray)
                .size(
                    R.styleable.MentionListView_streamUiSenderNameTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium)
                )
                .color(
                    R.styleable.MentionListView_streamUiSenderNameTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )
                .font(
                    R.styleable.MentionListView_streamUiSenderNameTextFontAssets,
                    R.styleable.MentionListView_streamUiSenderNameTextFont
                )
                .style(
                    R.styleable.MentionListView_streamUiSenderNameTextStyle,
                    Typeface.NORMAL
                )
                .build()

            val messageTextStyle = TextStyle.Builder(typedArray)
                .size(
                    R.styleable.MentionListView_streamUiMessageTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium)
                )
                .color(
                    R.styleable.MentionListView_streamUiMessageTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )
                .font(
                    R.styleable.MentionListView_streamUiMessageTextFontAssets,
                    R.styleable.MentionListView_streamUiMessageTextFont
                )
                .style(
                    R.styleable.MentionListView_streamUiMessageTextStyle,
                    Typeface.NORMAL
                )
                .build()

            val messageTimeTextStyle = TextStyle.Builder(typedArray)
                .size(
                    R.styleable.MentionListView_streamUiMessageTimeTextSize,
                    context.getDimension(R.dimen.stream_ui_text_medium)
                )
                .color(
                    R.styleable.MentionListView_streamUiMessageTimeTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )
                .font(
                    R.styleable.MentionListView_streamUiMessageTimeTextFontAssets,
                    R.styleable.MentionListView_streamUiMessageTimeTextFont
                )
                .style(
                    R.styleable.MentionListView_streamUiMessageTimeTextStyle,
                    Typeface.NORMAL
                )
                .build()

            val previewConfig = MessagePreviewView.PreviewStyle(
                messageSenderTextStyle = senderTextStyle,
                messageTextStyle = messageTextStyle,
                messageTimeTextStyle = messageTimeTextStyle
            )

            adapter.previewStyle = previewConfig
        }

        binding.mentionListRecyclerView.apply {
            setHasFixedSize(true)
            adapter = this@MentionListView.adapter
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
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
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
        Toast.makeText(context, R.string.stream_ui_mention_list_error, Toast.LENGTH_SHORT).show()
    }

    public fun setMentionSelectedListener(mentionSelectedListener: MentionSelectedListener?) {
        adapter.setMentionSelectedListener(mentionSelectedListener)
    }

    public fun setLoadMoreListener(loadMoreListener: LoadMoreListener?) {
        this.loadMoreListener = loadMoreListener
    }

    public fun interface MentionSelectedListener {
        public fun onMentionSelected(message: Message)
    }

    public fun interface LoadMoreListener {
        public fun onLoadMoreRequested()
    }
}
