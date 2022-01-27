package io.getstream.chat.android.ui.message.composer.suggestions.command

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding
import io.getstream.chat.android.ui.message.composer.MessageComposerChild

/**
 * Default implementation of command suggestions view. Displayed in a popup above [MessageComposerView] when there are
 * command suggestions available in the current [MessageComposerState].
 */
public class DefaultMessageComposerCommandSuggestionsContent : FrameLayout, MessageComposerChild {

    /**
     * Handle to layout binding.
     */
    private lateinit var binding: StreamUiSuggestionListViewBinding

    /**
     * Callback invoked when commands suggestion is selected.
     */
    public var commandSelectionListener: (Command) -> Unit = {}

    /**
     * Adapter used to render command suggestions.
     */
    private val adapter = CommandsAdapter { commandSelectionListener(it) }

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    /**
     * Sets up initial layout state and initializes suggestions RecyclerView.
     */
    private fun init() {
        binding = StreamUiSuggestionListViewBinding.inflate(streamThemeInflater, this)
        binding.suggestionsCardView.isVisible = true
        binding.suggestionsRecyclerView.adapter = adapter
    }

    /**
     * Propagates list of currently available command suggestions to adapter.
     *
     * @param state Current [MessageComposerState] instance. Provides available command suggestions data.
     */
    override fun renderState(state: MessageComposerState) {
        adapter.setItems(state.commandSuggestions)
    }
}
