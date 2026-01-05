/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionBinding
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.internal.SimpleListAdapter

/**
 * Represents the mention suggestion list popup shown above [MessageComposerView].
 */
public interface MessageComposerMentionSuggestionsContent : MessageComposerContent {
    /**
     * Selection listener invoked when a mention is selected.
     */
    public var mentionSelectionListener: ((User) -> Unit)?
}

/**
 * Represents the default mention suggestion list popup shown above [MessageComposerView].
 */
public open class DefaultMessageComposerMentionSuggestionsContent :
    FrameLayout,
    MessageComposerMentionSuggestionsContent {
    /**
     * Generated binding class for the XML layout.
     */
    protected lateinit var binding: StreamUiSuggestionListViewBinding

    /**
     * The style for [MessageComposerView].
     */
    protected lateinit var style: MessageComposerViewStyle

    /**
     * Adapter used to render mention suggestions.
     */
    protected lateinit var adapter: MentionSuggestionsAdapter

    /**
     * Selection listener invoked when a mention is selected.
     */
    public override var mentionSelectionListener: ((User) -> Unit)? = null

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init()
    }

    /**
     * Initializes the initial layout of the view.
     */
    private fun init() {
        binding = StreamUiSuggestionListViewBinding.inflate(streamThemeInflater, this)
        binding.suggestionsCardView.isVisible = true
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun <T, VH> buildAdapter(
        style: MessageComposerViewStyle,
    ): T where T : RecyclerView.Adapter<VH>, T : MentionSuggestionsAdapter, VH : RecyclerView.ViewHolder {
        return MentionsAdapter(style) { mentionSelectionListener?.invoke(it) } as T
    }

    /**
     * Initializes the content view with [MessageComposerContext].
     *
     * @param messageComposerContext The context of this [MessageComposerView].
     */
    override fun attachContext(messageComposerContext: MessageComposerContext) {
        this.style = messageComposerContext.style
        val rvAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> = buildAdapter(messageComposerContext.style)
        adapter = rvAdapter as MentionSuggestionsAdapter
        binding.suggestionsRecyclerView.adapter = rvAdapter
        binding.suggestionsCardView.setCardBackgroundColor(style.mentionSuggestionsBackgroundColor)
    }

    /**
     * Invoked when the state has changed and the UI needs to be updated accordingly.
     *
     * @param state The state that will be used to render the updated UI.
     */
    override fun renderState(state: MessageComposerState) {
        adapter.setItems(state.mentionSuggestions)
    }
}

/**
 * Adapter used to render mention suggestions.
 */
public interface MentionSuggestionsAdapter {

    /**
     * Sets the list of mention suggestions to be displayed.
     */
    public fun setItems(items: List<User>)

    /**
     * Returns the number of items in the adapter.
     */
    public fun getItemCount(): Int
}

/**
 * [RecyclerView.Adapter] responsible for displaying mention suggestions in a RecyclerView.
 *
 * @param style The style for [MessageComposerView].
 * @param mentionSelectionListener The listener invoked when a mention is selected from the list.
 */
private class MentionsAdapter(
    private val style: MessageComposerViewStyle,
    private val mentionSelectionListener: (User) -> Unit,
) : SimpleListAdapter<User, MentionsViewHolder>(), MentionSuggestionsAdapter {

    /**
     * Creates and instantiates a new instance of [MentionsViewHolder].
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new [MentionsViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionsViewHolder {
        return StreamUiItemMentionBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { MentionsViewHolder(it, style, mentionSelectionListener) }
    }
}

/**
 * [RecyclerView.ViewHolder] used for rendering mention items.
 *
 * @param binding Generated binding class for the XML layout.
 * @param style The style for [MessageComposerView].
 * @param mentionSelectionListener The listener invoked when a mention is selected.
 */
private class MentionsViewHolder(
    val binding: StreamUiItemMentionBinding,
    style: MessageComposerViewStyle,
    val mentionSelectionListener: (User) -> Unit,
) : SimpleListAdapter.ViewHolder<User>(binding.root) {

    private lateinit var item: User

    /**
     * The template string for the mention item with user name placeholder.
     */
    private val mentionTemplateText = style.mentionSuggestionItemMentionText

    init {
        binding.root.setOnClickListener { mentionSelectionListener(item) }
        binding.usernameTextView.setTextStyle(style.mentionSuggestionItemUsernameTextStyle)
        binding.mentionNameTextView.setTextStyle(style.mentionSuggestionItemMentionTextStyle)
        binding.mentionsIcon.setImageDrawable(
            style.mentionSuggestionItemIconDrawable.applyTint(
                tintColor = style.mentionSuggestionItemIconDrawableTintColor ?: style.buttonIconDrawableTintColor,
            ),
        )
    }

    /**
     * Updates [itemView] elements for a given [User] object.
     *
     * @param item Single mention suggestion represented by [User] class.
     */
    override fun bind(item: User) {
        this.item = item

        // Workaround for race condition caused by Coil trying to load stale avatar on layout.
        binding.userAvatarView.doOnLayout {
            binding.userAvatarView.setUser(item)
        }
        val username = String.format(mentionTemplateText, item.id.lowercase())
        binding.usernameTextView.text = item.name.ifEmpty { username }
        binding.mentionNameTextView.isVisible = item.name.isNotEmpty()
        binding.mentionNameTextView.text = username
    }
}
