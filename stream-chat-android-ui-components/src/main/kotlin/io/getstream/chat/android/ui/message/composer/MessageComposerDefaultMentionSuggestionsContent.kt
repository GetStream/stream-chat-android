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
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
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
        adapter.setItems(state.mentionSuggestions)
    }
}

/**
 * [RecyclerView.Adapter] responsible for displaying mention suggestions in the RecyclerView in [DefaultMentionSuggestionsContent].
 */
internal class MentionsAdapter(private inline val onMentionSelected: (User) -> Unit) :
    SimpleListAdapter<User, MentionsViewHolder>() {

    /**
     * Inflates layout and instantiates [MentionsViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionsViewHolder {
        val binding = StreamUiItemMentionBinding.inflate(parent.streamThemeInflater, parent, false)
        return MentionsViewHolder(binding, onMentionSelected)
    }
}

/**
 * [RecyclerView.ViewHolder] used for rendering mention. Used by [MentionsAdapter].
 *
 * @param binding Handle to [StreamUiItemMentionBinding] instance.
 */
internal class MentionsViewHolder(val binding: StreamUiItemMentionBinding, val onMentionSelected: (User) -> Unit) :
    SimpleListAdapter.ViewHolder<User>(binding.root) {

    init {
        setIsRecyclable(false)
    }

    /**
     * Updates [itemView] elements for a given [User] object.
     *
     * @param user Single mention suggestion represented by [User] class.
     */
    override fun bind(user: User) {
        binding.apply {
            root.setOnClickListener { onMentionSelected(user) }
            avatarView.setUserData(user)
            usernameTextView.text = user.name
            mentionNameTextView.text = itemView.context.getString(
                R.string.stream_ui_mention,
                user.name.lowercase()
            )
        }
    }
}
