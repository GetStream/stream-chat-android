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

package io.getstream.chat.android.ui.suggestion.list

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.setStartDrawable
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding
import io.getstream.chat.android.ui.suggestion.Suggestions
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItem
import io.getstream.chat.android.ui.suggestion.list.adapter.SuggestionListItemViewHolderFactory
import io.getstream.chat.android.ui.suggestion.list.adapter.internal.CommandListAdapter
import io.getstream.chat.android.ui.suggestion.list.adapter.internal.MentionListAdapter

/**
 * A View that shows a list of suggestions.
 */
public class SuggestionListView : FrameLayout, SuggestionListUi {

    /**
     * A ViewBinding generated binding for [io.getstream.chat.android.ui.R.layout.stream_ui_suggestion_list_view].
     */
    private val binding: StreamUiSuggestionListViewBinding =
        StreamUiSuggestionListViewBinding.inflate(streamThemeInflater, this)

    /**
     * Creates ViewHolders for mention and command suggestion items.
     */
    private var viewHolderFactory: SuggestionListItemViewHolderFactory = SuggestionListItemViewHolderFactory()

    /**
     * Creates a mention suggestions list adapter for the RecyclerView.
     */
    private val mentionListAdapter: MentionListAdapter = MentionListAdapter(::viewHolderFactory) {
        suggestionClickListener?.onMentionClick(it)
    }

    /**
     * Creates a command suggestions list adapter for the RecyclerView.
     */
    private val commandListAdapter: CommandListAdapter = CommandListAdapter(::viewHolderFactory) {
        suggestionClickListener?.onCommandClick(it)
    }

    /**
     * Style used by the suggestion list.
     */
    private lateinit var style: SuggestionListViewStyle

    /**
     * A listener that handles clicks on suggestion items.
     */
    private var suggestionClickListener: OnSuggestionClickListener? = null

    public constructor(context: Context) : this(context, null, 0)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    )

    /**
     * Initializes the View by setting the style for the suggestion list and
     * initializing the RecyclerView.
     */
    init {
        setSuggestionListViewStyle(SuggestionListViewStyle.createDefault(context))
        binding.suggestionsRecyclerView.apply {
            itemAnimator = null
            adapter = ConcatAdapter(mentionListAdapter, commandListAdapter)
        }
    }

    /**
     * A setter for [viewHolderFactory].
     *
     * Use this if you want to customize the UI by providing your own [SuggestionListItemViewHolderFactory].
     */
    public fun setSuggestionListViewHolderFactory(viewHolderFactory: SuggestionListItemViewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory.also { it.style = style }
    }

    /**
     * Sets the style for the suggestion list.
     */
    internal fun setSuggestionListViewStyle(style: SuggestionListViewStyle) {
        this.style = style

        binding.suggestionsCardView.setCardBackgroundColor(style.suggestionsBackground)
        binding.commandsTitleTextView.setTextStyle(style.commandsTitleTextStyle)
        binding.commandsTitleTextView.setStartDrawable(style.lightningIcon)
        viewHolderFactory.style = style
    }

    /**
     * A setter for [suggestionClickListener].
     *
     * Use this if you want to implement custom on item clicked behavior.
     */
    public fun setOnSuggestionClickListener(suggestionClickListener: OnSuggestionClickListener) {
        this.suggestionClickListener = suggestionClickListener
    }

    /**
     * Renders the list of suggestions if the suggestion dataset is not empty.
     * If the dataset is empty the list of suggestions is hidden.
     */
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
        }
    }

    /**
     * Shows if the suggestion list is currently visible.
     */
    override fun isSuggestionListVisible(): Boolean {
        return binding.suggestionsCardView.isVisible
    }

    /**
     * Hides the suggestion list.
     */
    private fun hideSuggestionList() {
        if (binding.suggestionsCardView.isVisible) {
            commandListAdapter.clear()
            mentionListAdapter.clear()
            binding.suggestionsCardView.isVisible = false
        }
    }

    /**
     * A listener used to handle clicks on different types of suggestion items.
     */
    public interface OnSuggestionClickListener {

        /**
         * Handles clicks on mention suggestion items.
         */
        public fun onMentionClick(user: User)

        /**
         * Handles Clicks on command suggestion items.
         */
        public fun onCommandClick(command: Command)
    }
}
