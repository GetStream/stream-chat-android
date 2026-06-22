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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionSpecialBinding
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerContext
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.common.R as UiCommonR

/**
 * Represents the mention suggestion list popup shown above [MessageComposerView].
 */
public interface MessageComposerMentionSuggestionsContent : MessageComposerContent {
    /**
     * Selection listener invoked when a user mention is selected.
     *
     * Kept for backward compatibility; only fires for [Mention.User] rows. New code should
     * prefer [suggestedMentionSelectionListener], which fires for every mention type. Note both
     * listeners fire on a user tap: a custom [mentionSelectionListener] runs in addition to
     * [suggestedMentionSelectionListener], so the default selection still inserts the mention. To
     * replace the default selection behavior, override [suggestedMentionSelectionListener].
     */
    @Deprecated(
        message = "Use suggestedMentionSelectionListener; it also fires for other mention types.",
        replaceWith = ReplaceWith("suggestedMentionSelectionListener"),
        level = DeprecationLevel.WARNING,
    )
    public var mentionSelectionListener: ((User) -> Unit)?

    /**
     * Selection listener invoked when any mention is selected.
     *
     * No-op default getter/setter for backward compatibility.
     */
    public var suggestedMentionSelectionListener: ((Mention) -> Unit)?
        get() = null
        set(_) = Unit
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

    @Deprecated(
        message = "Use suggestedMentionSelectionListener; it also fires for other mention types.",
        replaceWith = ReplaceWith("suggestedMentionSelectionListener"),
        level = DeprecationLevel.WARNING,
    )
    public override var mentionSelectionListener: ((User) -> Unit)? = null

    public override var suggestedMentionSelectionListener: ((Mention) -> Unit)? = null

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
        return MentionsAdapter(style = style, onMentionSelected = ::onMentionSelected) as T
    }

    /**
     * Fires the (Mention)-typed listener for every row, and the deprecated user-only listener
     * when the picked mention is a [Mention.User], so existing callers keep working.
     */
    private fun onMentionSelected(mention: Mention) {
        suggestedMentionSelectionListener?.invoke(mention)
        if (mention is Mention.User) {
            mentionSelectionListener?.invoke(mention.user)
        }
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
        adapter.setMentions(state.suggestedMentions)
    }
}

/**
 * Adapter used to render mention suggestions.
 */
public interface MentionSuggestionsAdapter {

    /**
     * Sets the list of user mention suggestions to be displayed.
     *
     * Kept for backward compatibility; only sees [Mention.User] entries. Override [setMentions]
     * to also handle other mention types.
     */
    public fun setItems(items: List<User>)

    /**
     * Sets the heterogeneous mention suggestion list.
     *
     * The default implementation filters [Mention.User] entries and delegates to [setItems], so
     * adapters that only override [setItems] keep working — they simply ignore non-user mentions.
     */
    public fun setMentions(items: List<Mention>) {
        setItems(items.mapNotNull { (it as? Mention.User)?.user })
    }

    /**
     * Returns the number of items in the adapter.
     */
    public fun getItemCount(): Int
}

/**
 * [RecyclerView.Adapter] responsible for displaying heterogeneous mention suggestions.
 *
 * Renders [Mention.User] rows with the existing user-mention layout and every other [Mention]
 * variant with a simpler icon-plus-name row.
 */
private class MentionsAdapter(
    private val style: MessageComposerViewStyle,
    private val onMentionSelected: (Mention) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), MentionSuggestionsAdapter {

    private val items: MutableList<Mention> = mutableListOf()

    override fun setItems(items: List<User>): Unit = setMentions(items.map(Mention::User))

    @SuppressLint("NotifyDataSetChanged")
    override fun setMentions(items: List<Mention>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Mention.User -> VIEW_TYPE_USER
        else -> VIEW_TYPE_SPECIAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        VIEW_TYPE_USER ->
            StreamUiItemMentionBinding
                .inflate(parent.streamThemeInflater, parent, false)
                .let { UserMentionsViewHolder(it, style, onMentionSelected) }

        else ->
            StreamUiItemMentionSpecialBinding
                .inflate(parent.streamThemeInflater, parent, false)
                .let { SpecialMentionsViewHolder(it, style, onMentionSelected) }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is UserMentionsViewHolder -> holder.bind(item as Mention.User)
            is SpecialMentionsViewHolder -> holder.bind(item)
        }
    }

    private companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_SPECIAL = 1
    }
}

/**
 * [RecyclerView.ViewHolder] used for rendering user mention items.
 */
private class UserMentionsViewHolder(
    val binding: StreamUiItemMentionBinding,
    style: MessageComposerViewStyle,
    val onMentionSelected: (Mention) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: Mention.User

    private val mentionTemplateText = style.mentionSuggestionItemMentionText

    init {
        binding.root.setOnClickListener { onMentionSelected(item) }
        binding.usernameTextView.setTextStyle(style.mentionSuggestionItemUsernameTextStyle)
        binding.mentionNameTextView.setTextStyle(style.mentionSuggestionItemMentionTextStyle)
        binding.mentionsIcon.setImageDrawable(
            style.mentionSuggestionItemIconDrawable.applyTint(
                tintColor = style.mentionSuggestionItemIconDrawableTintColor,
            ),
        )
    }

    fun bind(item: Mention.User) {
        this.item = item
        val user = item.user

        // Workaround for race condition caused by Coil trying to load stale avatar on layout.
        binding.userAvatarView.doOnLayout {
            binding.userAvatarView.setUser(user)
        }
        val username = String.format(mentionTemplateText, user.id.lowercase())
        binding.usernameTextView.text = user.name.ifEmpty { username }
        binding.mentionNameTextView.isVisible = user.name.isNotEmpty()
        binding.mentionNameTextView.text = username
    }
}

/**
 * [RecyclerView.ViewHolder] used for rendering non-user mention items.
 *
 * Until the design system covers custom mentions, an unknown [Mention] type falls back to a neutral
 * icon and the [Mention.display] string as the row label.
 */
private class SpecialMentionsViewHolder(
    val binding: StreamUiItemMentionSpecialBinding,
    style: MessageComposerViewStyle,
    val onMentionSelected: (Mention) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: Mention

    init {
        binding.root.setOnClickListener { onMentionSelected(item) }
        binding.nameTextView.setTextStyle(style.mentionSuggestionItemUsernameTextStyle)
        binding.subtitleTextView.setTextStyle(style.mentionSuggestionItemMentionTextStyle)
    }

    fun bind(item: Mention) {
        this.item = item
        binding.iconImageView.setImageResource(item.iconRes())
        binding.nameTextView.text = "@${item.display}"
        val subtitle = item.subtitle(binding.root.context)
        binding.subtitleTextView.text = subtitle.orEmpty()
        binding.subtitleTextView.isVisible = subtitle != null
    }

    private fun Mention.iconRes(): Int = when (this) {
        Mention.Channel, Mention.Here -> UiCommonR.drawable.stream_design_ic_megaphone
        is Mention.Role -> UiCommonR.drawable.stream_design_ic_role
        is Mention.Group -> UiCommonR.drawable.stream_design_ic_users
        else -> UiCommonR.drawable.stream_design_ic_users
    }

    private fun Mention.subtitle(context: Context): String? = when (this) {
        Mention.Channel -> context.getString(
            R.string.stream_ui_message_composer_mention_suggestion_channel_subtitle,
        )
        Mention.Here -> context.getString(
            R.string.stream_ui_message_composer_mention_suggestion_here_subtitle,
        )
        is Mention.Role -> context.getString(
            R.string.stream_ui_message_composer_mention_suggestion_role_subtitle,
            role,
        )
        is Mention.Group -> group.description?.takeIf { it.isNotBlank() }
        else -> null
    }
}
