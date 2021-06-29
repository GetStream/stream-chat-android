package io.getstream.chat.android.ui.suggestion.list

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawable
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding
import io.getstream.chat.android.ui.suggestion.Suggestions
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.internal.CommandListAdapter
import io.getstream.chat.android.ui.suggestion.list.adapter.internal.MentionListAdapter
import io.getstream.chat.android.ui.suggestion.list.internal.SuggestionListUi

public class SuggestionListView : FrameLayout, SuggestionListUi {

    private val binding: StreamUiSuggestionListViewBinding =
        StreamUiSuggestionListViewBinding.inflate(streamThemeInflater, this)
    private var viewHolderFactory: SuggestionListItemViewHolderFactory = SuggestionListItemViewHolderFactory()

    private val mentionListAdapter: MentionListAdapter = MentionListAdapter(::viewHolderFactory) {
        suggestionClickListener?.onMentionClick(it)
    }
    private val commandListAdapter: CommandListAdapter = CommandListAdapter(::viewHolderFactory) {
        suggestionClickListener?.onCommandClick(it)
    }

    private var style: SuggestionListViewStyle? = null
    private var suggestionClickListener: OnSuggestionClickListener? = null

    public constructor(context: Context) : this(context, null, 0)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    )

    init {
        binding.suggestionsRecyclerView.apply {
            itemAnimator = null
            adapter = ConcatAdapter(mentionListAdapter, commandListAdapter)
        }
    }

    public fun setSuggestionListViewHolderFactory(viewHolderFactory: SuggestionListItemViewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory.also { it.style = style }
    }

    internal fun setSuggestionListViewStyle(style: SuggestionListViewStyle) {
        this.style = style

        binding.suggestionsCardView.setCardBackgroundColor(style.suggestionsBackground)
        binding.commandsTitleTextView.setTextStyle(style.commandsTitleTextStyle)
        binding.commandsTitleTextView.setLeftDrawable(style.lightningIcon)
        viewHolderFactory.style = style
    }

    public fun setOnSuggestionClickListener(suggestionClickListener: OnSuggestionClickListener) {
        this.suggestionClickListener = suggestionClickListener
    }

    override fun renderSuggestions(suggestions: Suggestions) {
        binding.suggestionsCardView.isVisible = true
        when (suggestions) {
            is Suggestions.MentionSuggestions -> {
                if (suggestions.users.isEmpty()) {
                    hideSuggestionList()
                } else {
                    mentionListAdapter.setItems(suggestions.users.map(SuggestionListItem::MentionItem))
                    binding.commandsTitleTextView.isVisible = false
                }
            }
            is Suggestions.CommandSuggestions -> {
                if (suggestions.commands.isEmpty()) {
                    hideSuggestionList()
                } else {
                    commandListAdapter.setItems(suggestions.commands.map(SuggestionListItem::CommandItem))
                    binding.commandsTitleTextView.isVisible = true
                }
            }
            is Suggestions.EmptySuggestions -> hideSuggestionList()
        }.exhaustive
    }

    override fun isSuggestionListVisible(): Boolean {
        return binding.suggestionsCardView.isVisible
    }

    private fun hideSuggestionList() {
        if (binding.suggestionsCardView.isVisible) {
            commandListAdapter.clear()
            mentionListAdapter.clear()
            binding.suggestionsCardView.isVisible = false
        }
    }

    public interface OnSuggestionClickListener {
        public fun onMentionClick(user: User)

        public fun onCommandClick(command: Command)
    }
}
