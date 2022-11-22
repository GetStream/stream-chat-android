/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.composer.content

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.databinding.StreamUiItemCommandBinding
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.setStartDrawable
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.internal.SimpleListAdapter

/**
 * Represents the default command suggestion list popup shown above [MessageComposerView].
 */
public class DefaultMessageComposerCommandSuggestionsContent : FrameLayout, MessageComposerContent {
    /**
     * Generated binding class for the XML layout.
     */
    private lateinit var binding: StreamUiSuggestionListViewBinding

    /**
     * The style for [MessageComposerView].
     */
    private lateinit var style: MessageComposerViewStyle

    /**
     * Adapter used to render command suggestions.
     */
    private val adapter: CommandsAdapter by lazy {
        CommandsAdapter(style) { commandSelectionListener(it) }
    }

    /**
     * Selection listener invoked when a command is selected.
     */
    public var commandSelectionListener: (Command) -> Unit = {}

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init()
    }

    /**
     * Initializes the initial layout of the view.
     */
    private fun init() {
        binding = StreamUiSuggestionListViewBinding.inflate(streamThemeInflater, this)
        binding.suggestionsCardView.isVisible = true
        binding.commandsTitleTextView.isVisible = true
    }

    /**
     * Initializes the content view with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style

        binding.suggestionsRecyclerView.adapter = adapter
        binding.suggestionsCardView.setCardBackgroundColor(style.commandSuggestionsBackgroundColor)
        binding.commandsTitleTextView.text = style.commandSuggestionsTitleText
        binding.commandsTitleTextView.setTextStyle(style.commandSuggestionsTitleTextStyle)
        binding.commandsTitleTextView.setStartDrawable(
            style.commandSuggestionsTitleIconDrawable.applyTint(style.buttonIconDrawableTintColor)
        )
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
 * @param style The style for [MessageComposerView].
 * @param commandSelectionListener The listener invoked when a command is selected from the list.
 */
private class CommandsAdapter(
    private val style: MessageComposerViewStyle,
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
            .let { CommandViewHolder(it, style, commandSelectionListener) }
    }
}

/**
 * [RecyclerView.ViewHolder] used for rendering command items.
 *
 * @param binding Generated binding class for the XML layout.
 * @param style The style for [MessageComposerView].
 * @param commandSelectionListener The listener invoked when a command is selected.
 */
private class CommandViewHolder(
    private val binding: StreamUiItemCommandBinding,
    style: MessageComposerViewStyle,
    private val commandSelectionListener: (Command) -> Unit,
) : SimpleListAdapter.ViewHolder<Command>(binding.root) {

    private lateinit var item: Command

    /**
     * The template string for the command description with two placeholders for command name
     * and arguments.
     */
    private val commandTemplateText = style.commandSuggestionItemCommandDescriptionText

    init {
        binding.root.setOnClickListener { commandSelectionListener(item) }
        binding.commandNameTextView.setTextStyle(style.commandSuggestionItemCommandNameTextStyle)
        binding.commandQueryTextView.setTextStyle(style.commandSuggestionItemCommandDescriptionTextStyle)
        binding.instantCommandImageView.isVisible = false
    }

    /**
     * Updates [itemView] elements for a given [Command] object.
     *
     * @param item Single command suggestion represented by [Command] class.
     */
    override fun bind(item: Command) {
        this.item = item

        binding.commandNameTextView.text = item.name.replaceFirstChar(Char::uppercase)
        binding.commandQueryTextView.text = String.format(commandTemplateText, item.name, item.args)
    }
}
