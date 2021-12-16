package io.getstream.chat.android.ui.message.composer

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMentionBinding
import io.getstream.chat.android.ui.databinding.StreamUiSuggestionListViewBinding

/**
 * Default implementation of mention suggestions view. Displayed in a popup above [MessageComposerView] when there are
 * mention suggestions available in the current [MessageInputState].
 */
internal class DefaultMentionSuggestionsContent : FrameLayout, MessageComposerChild {

    /**
     * Handle to layout binding.
     */
    private val binding: StreamUiSuggestionListViewBinding =
        StreamUiSuggestionListViewBinding.inflate(streamThemeInflater, this)

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

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init()
    }

    /**
     * Set up initial layout state.
     */
    private fun init() {
        binding.apply {
            suggestionsCardView.isVisible = true
            commandsTitleTextView.isVisible = false
            suggestionsRecyclerView.adapter = adapter
        }
    }

    /**
     * Propagates list of currently available mention suggestions to adapter.
     */
    override fun renderState(state: MessageInputState) {
        adapter.setMentions(state.mentionSuggestions)
    }
}

/**
 * [RecyclerView.Adapter] responsible for displaying mention suggestions in the RecyclerView in [DefaultMentionSuggestionsContent].
 */
internal class MentionsAdapter(inline val onMentionSelected: (User) -> Unit) : RecyclerView.Adapter<MentionsViewHolder>() {
    private val mentions: MutableList<User> = mutableListOf()

    /**
     * Updates mentions data and re-renders list.
     */
    fun setMentions(mentions: List<User>) {
        this.mentions.apply {
            clear()
            addAll(mentions)
            notifyDataSetChanged()
        }
    }

    /**
     * Inflates layout and instantiates [MentionsViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionsViewHolder {
        val binding = StreamUiItemMentionBinding.inflate(parent.streamThemeInflater, parent, false)
        return MentionsViewHolder(binding)
    }

    /**
     * Invokes [MentionsViewHolder] to refresh its [MentionsViewHolder.itemView].
     */
    override fun onBindViewHolder(holder: MentionsViewHolder, position: Int) {
        val user = mentions[position]
        holder.bind(user)
        holder.itemView.setOnClickListener { onMentionSelected(user) }
    }

    /**
     * @return Size of mentions list.
     */
    override fun getItemCount(): Int {
        return mentions.size
    }
}

/**
 * [RecyclerView.ViewHolder] used for rendering mention. Used by [MentionsAdapter].
 *
 * @param binding Handle to [StreamUiItemMentionBinding] instance.
 */
internal class MentionsViewHolder(val binding: StreamUiItemMentionBinding) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Setups view holder properties.
     */
    init {
        // setting viewholder not recyclable, to enforce correct avatars refreshing.
        setIsRecyclable(false)
    }

    /**
     * Updates [itemView] elements for a given [User] object.
     *
     * @param user Single mention suggestion represented by [User] class.
     */
    internal fun bind(user: User) = binding.apply {
        avatarView.setUserData(user)
        usernameTextView.text = user.name
        mentionNameTextView.text = itemView.context.getString(
            R.string.stream_ui_mention,
            user.name.lowercase()
        )
    }
}
