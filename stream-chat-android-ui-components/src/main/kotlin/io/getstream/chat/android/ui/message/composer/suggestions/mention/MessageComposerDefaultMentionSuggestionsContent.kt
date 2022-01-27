package io.getstream.chat.android.ui.message.composer.suggestions.mention

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding
import io.getstream.chat.android.ui.message.composer.MessageComposerChild

/**
 * Default implementation of mention suggestions view. Displayed in a popup above [MessageComposerView] when there are
 * mention suggestions available in the current [MessageComposerState].
 */
internal class DefaultMentionSuggestionsContent : FrameLayout, MessageComposerChild {

    /**
     * Handle to layout binding.
     */
    private lateinit var binding: StreamUiSuggestionListViewBinding

    /**
     * Callback invoked when mention suggestion is selected.
     */
    public var onMentionSelected: (User) -> Unit = {}

    /**
     * Adapter used to render mention suggestions.
     */
    private val adapter = MentionsAdapter { onMentionSelected(it) }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    /**
     * Set up initial layout state.
     */
    private fun init() {
        binding = StreamUiSuggestionListViewBinding.inflate(streamThemeInflater, this)
        binding.suggestionsCardView.isVisible = true
        binding.commandsTitleTextView.isVisible = false
        binding.suggestionsRecyclerView.adapter = adapter
    }

    /**
     * Propagates list of currently available mention suggestions to adapter.
     *
     * @param state Current [MessageComposerState] instance. Provides available mention suggestions data.
     */
    override fun renderState(state: MessageComposerState) {
        adapter.setItems(state.mentionSuggestions)
    }
}
