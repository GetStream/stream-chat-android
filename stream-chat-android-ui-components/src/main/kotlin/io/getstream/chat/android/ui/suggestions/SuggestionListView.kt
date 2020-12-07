package io.getstream.chat.android.ui.suggestions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding

internal class SuggestionListView : FrameLayout {

    private val binding: StreamUiSuggestionListViewBinding = LayoutInflater.from(context).let {
        StreamUiSuggestionListViewBinding.inflate(it, this)
    }
    private val mentionsAdapter: MentionsAdapter = MentionsAdapter { listener?.onMentionClick(it) }
    private val commandsAdapter: CommandsAdapter = CommandsAdapter { listener?.onCommandClick(it) }

    private var listener: OnSuggestionClickListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        binding.suggestionsRecyclerView.apply {
            itemAnimator = null
            adapter = ConcatAdapter(mentionsAdapter, commandsAdapter)
        }
    }

    fun setSuggestions(suggestions: Suggestions) {
        binding.suggestionsCardView.isVisible = true
        when (suggestions) {
            is Suggestions.MentionSuggestions -> {
                if (suggestions.users.isEmpty()) {
                    clearSuggestions()
                } else {
                    mentionsAdapter.submitList(suggestions.users)
                    binding.commandsTitleTextView.isVisible = false
                }
            }
            is Suggestions.CommandSuggestions -> {
                if (suggestions.commands.isEmpty()) {
                    clearSuggestions()
                } else {
                    commandsAdapter.submitList(suggestions.commands)
                    binding.commandsTitleTextView.isVisible = true
                }
            }
        }
    }

    fun setOnSuggestionClickListener(listener: OnSuggestionClickListener) {
        this.listener = listener
    }

    fun clearSuggestions() {
        if (binding.suggestionsCardView.isVisible) {
            commandsAdapter.submitList(emptyList())
            mentionsAdapter.submitList(emptyList())
            binding.suggestionsCardView.isVisible = false
        }
    }

    interface OnSuggestionClickListener {
        fun onMentionClick(user: User)

        fun onCommandClick(command: Command)
    }

    sealed class Suggestions {
        data class MentionSuggestions(val users: List<User>) : Suggestions()
        data class CommandSuggestions(val commands: List<Command>) : Suggestions()
    }
}
