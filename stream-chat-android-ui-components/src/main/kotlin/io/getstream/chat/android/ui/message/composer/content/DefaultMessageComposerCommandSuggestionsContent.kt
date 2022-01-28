package io.getstream.chat.android.ui.message.composer.content

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.common.composer.MessageComposerState
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemCommandBinding
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding
import io.getstream.chat.android.ui.message.composer.MessageComposerComponent
import io.getstream.chat.android.ui.message.composer.MessageComposerView

/**
 * Represents the default command suggestion list popup shown above [MessageComposerView].
 */
public class DefaultMessageComposerCommandSuggestionsContent : FrameLayout, MessageComposerComponent {

    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiSuggestionListViewBinding

    /**
     * Selection listener invoked when a command is selected.
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
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        adapter.setItems(state.commandSuggestions)
    }
}

/**
 * [RecyclerView.Adapter] responsible for displaying command suggestions in a RecyclerView.
 *
 * @param commandSelectionListener The listener invoked when a command is selected from the list.
 */
private class CommandsAdapter(
    private val commandSelectionListener: (Command) -> Unit,
) : SimpleListAdapter<Command, CommandViewHolder>() {

    /**
     * Creates and instantiates a new instance of [CommandViewHolder].
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new [CommandViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        return StreamUiItemCommandBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { CommandViewHolder(it, commandSelectionListener) }
    }
}

/**
 * [RecyclerView.ViewHolder] used for rendering command items.
 *
 * @param binding Generated binding class for the XML layout.
 * @param commandSelectionListener The listener invoked when a command is selected.
 */
private class CommandViewHolder(
    private val binding: StreamUiItemCommandBinding,
    private val commandSelectionListener: (Command) -> Unit,
) : SimpleListAdapter.ViewHolder<Command>(binding.root) {

    /**
     * Updates [itemView] elements for a given [Command] object.
     *
     * @param item Single command suggestion represented by [Command] class.
     */
    override fun bind(item: Command) {
        binding.root.setOnClickListener { commandSelectionListener(item) }
        binding.commandNameTextView.text = item.name.replaceFirstChar(Char::uppercase)
        binding.commandQueryTextView.text = context.getString(
            R.string.stream_ui_message_input_command_template,
            item.name,
            item.args
        )
    }
}
