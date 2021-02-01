package io.getstream.chat.android.ui.messages.reactions.user

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiUserReactionsViewBinding
import io.getstream.chat.android.ui.utils.UiUtils

@InternalStreamChatApi
public class UserReactionsView : FrameLayout {

    private val binding = StreamUiUserReactionsViewBinding.inflate(context.inflater, this, true)

    private val userReactionsAdapter: UserReactionAdapter = UserReactionAdapter()
    private val gridLayoutManager: GridLayoutManager

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        binding.recyclerView.adapter = userReactionsAdapter
        gridLayoutManager = binding.recyclerView.layoutManager as GridLayoutManager
    }

    public fun setMessage(message: Message, currentUser: User) {
        bindTitle(message)
        bindReactionList(message, currentUser)
    }

    private fun bindTitle(message: Message) {
        val reactionCount = message.latestReactions.size
        binding.messageMembersTextView.text = context.resources.getQuantityString(
            R.plurals.stream_ui_user_reactions_title,
            reactionCount,
            reactionCount
        )
    }

    private fun bindReactionList(message: Message, currentUser: User) {
        val userReactionItems = message.latestReactions.mapNotNull {
            val user = it.user
            val iconDrawableRes = UiUtils.getReactionIcon(it.type)
            if (user != null && iconDrawableRes != null) {
                UserReactionItem(
                    user = user,
                    reaction = it,
                    isMine = user.id == currentUser.id,
                    iconDrawableRes = iconDrawableRes
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
