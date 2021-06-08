package io.getstream.chat.android.ui.message.list.reactions.user.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.supportedLatestReactions
import io.getstream.chat.android.ui.databinding.StreamUiUserReactionsViewBinding
import io.getstream.chat.android.ui.message.list.MessageListViewStyle

@InternalStreamChatApi
public class UserReactionsView : FrameLayout {

    private val binding = StreamUiUserReactionsViewBinding.inflate(streamThemeInflater, this, true)

    private val userReactionsAdapter: UserReactionAdapter = UserReactionAdapter()
    private val gridLayoutManager: GridLayoutManager

    public constructor(context: Context) : super(context.createStreamThemeWrapper())
    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context.createStreamThemeWrapper(), attrs, defStyleAttr)

    init {
        binding.recyclerView.adapter = userReactionsAdapter
        gridLayoutManager = binding.recyclerView.layoutManager as GridLayoutManager
    }

    public fun setMessage(message: Message, currentUser: User) {
        bindTitle(message)
        bindReactionList(message, currentUser)
    }

    internal fun configure(messageListViewStyle: MessageListViewStyle) {
        binding.userReactionsContainer.setCardBackgroundColor(messageListViewStyle.userReactionsBackgroundColor)
        messageListViewStyle.userReactionsTitleText.apply(binding.userReactionsTitleTextView)
    }

    private fun bindTitle(message: Message) {
        val reactionCount = message.supportedLatestReactions.size
        binding.userReactionsTitleTextView.text = context.resources.getQuantityString(
            R.plurals.stream_ui_message_list_message_reactions,
            reactionCount,
            reactionCount
        )
    }

    private fun bindReactionList(message: Message, currentUser: User) {
        val userReactionItems = message.supportedLatestReactions.mapNotNull {
            val user = it.user
            val reactionDrawable = ChatUI.supportedReactions.getReactionDrawable(it.type)
            if (user != null && reactionDrawable != null) {
                UserReactionItem(
                    user = user,
                    reaction = it,
                    isMine = user.id == currentUser.id,
                    reactionDrawable = reactionDrawable
                )
            } else {
                null
            }
        }

        gridLayoutManager.spanCount = userReactionItems.size.coerceAtMost(MAX_COLUMNS_COUNT)
        userReactionsAdapter.submitList(userReactionItems)
    }

    private companion object {
        private const val MAX_COLUMNS_COUNT = 4
    }
}
