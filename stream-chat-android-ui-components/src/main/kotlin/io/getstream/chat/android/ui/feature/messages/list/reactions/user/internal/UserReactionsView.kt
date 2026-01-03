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

package io.getstream.chat.android.ui.feature.messages.list.reactions.user.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.list.getUserReactionAlignment
import io.getstream.chat.android.ui.databinding.StreamUiUserReactionsViewBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.extensions.supportedLatestReactions

@InternalStreamChatApi
public class UserReactionsView : FrameLayout {

    private val binding = StreamUiUserReactionsViewBinding.inflate(streamThemeInflater, this, true)

    private val userReactionsAdapter: UserReactionAdapter = UserReactionAdapter {
        userReactionClickListener?.onUserReactionClick(it.user, it.reaction)
    }
    private val gridLayoutManager: GridLayoutManager

    private var userReactionClickListener: UserReactionClickListener? = null

    public constructor(context: Context) : this(context, null, 0)
    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    )

    init {
        binding.recyclerView.adapter = userReactionsAdapter
        gridLayoutManager = binding.recyclerView.layoutManager as GridLayoutManager
    }

    public fun setMessage(message: Message, currentUser: User) {
        bindTitle(message)
        bindReactionList(message, currentUser)
    }

    internal fun setOnUserReactionClickListener(userReactionClickListener: UserReactionClickListener) {
        this.userReactionClickListener = userReactionClickListener
    }

    internal fun configure(messageListViewStyle: MessageListViewStyle) {
        binding.userReactionsContainer.setCardBackgroundColor(messageListViewStyle.userReactionsBackgroundColor)
        binding.userReactionsTitleTextView.setTextStyle(messageListViewStyle.userReactionsTitleText)
        userReactionsAdapter.messageOptionsUserReactionAlignment = messageListViewStyle
            .messageOptionsUserReactionAlignment.getUserReactionAlignment()
    }

    private fun bindTitle(message: Message) {
        val reactionCount = message.supportedLatestReactions.size
        binding.userReactionsTitleTextView.text = context.resources.getQuantityString(
            R.plurals.stream_ui_message_list_message_reactions,
            reactionCount,
            reactionCount,
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
                    reactionDrawable = reactionDrawable,
                )
            } else {
                null
            }
        }

        gridLayoutManager.spanCount = userReactionItems.size.coerceAtMost(MAX_COLUMNS_COUNT)
        userReactionsAdapter.submitList(userReactionItems)
    }

    internal fun interface UserReactionClickListener {
        fun onUserReactionClick(user: User, reaction: Reaction)
    }

    private companion object {
        private const val MAX_COLUMNS_COUNT = 4
    }
}
