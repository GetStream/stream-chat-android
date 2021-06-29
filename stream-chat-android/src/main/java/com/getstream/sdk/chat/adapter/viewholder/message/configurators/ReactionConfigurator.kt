package com.getstream.sdk.chat.adapter.viewholder.message.configurators

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.ReactionListItemAdapter
import com.getstream.sdk.chat.adapter.viewholder.message.getActiveContentViewResId
import com.getstream.sdk.chat.adapter.viewholder.message.isDeleted
import com.getstream.sdk.chat.adapter.viewholder.message.isFailed
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.utils.UiUtils
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel
import top.defaults.drawabletoolbox.DrawableBuilder

internal class ReactionConfigurator(
    private val binding: StreamItemMessageBinding,
    private val context: Context,
    private val style: MessageListViewStyle,
    private val channel: Channel,
    private val reactionViewClickListener: MessageListView.ReactionViewClickListener,
    private val configParamsReadIndicator: (MessageItem) -> Unit
) : Configurator {

    override fun configure(messageItem: MessageItem) {
        configReactionView(messageItem)
        configParamsReactionSpace(messageItem)
        configParamsReactionTail(messageItem)
        configParamsReactionRecyclerView(messageItem)
    }

    private fun configReactionView(messageItem: MessageItem) {
        val message = messageItem.message

        if (message.isDeleted() ||
            message.isFailed() ||
            !style.isReactionEnabled ||
            !channel.config.isReactionsEnabled ||
            message.reactionCounts.isEmpty()
        ) {
            binding.reactionsRecyclerView.visibility = View.GONE
            binding.ivTail.visibility = View.GONE
            binding.spaceReactionTail.visibility = View.GONE
            return
        }

        configStyleReactionView(messageItem)

        binding.reactionsRecyclerView.visibility = View.VISIBLE
        binding.ivTail.visibility = View.VISIBLE
        binding.spaceReactionTail.visibility = View.VISIBLE
        binding.reactionsRecyclerView.adapter = ReactionListItemAdapter(
            context,
            message.reactionCounts,
            UiUtils.getReactionTypes(),
            style
        )
        binding.reactionsRecyclerView.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                reactionViewClickListener.onReactionViewClick(message)
            }
            false
        }
    }

    private fun configStyleReactionView(messageItem: MessageItem) {
        if (style.reactionViewBgDrawable == -1) {
            binding.reactionsRecyclerView.background = DrawableBuilder()
                .rectangle()
                .rounded()
                .solidColor(style.reactionViewBgColor)
                .solidColorPressed(Color.LTGRAY)
                .build()

            binding.ivTail.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    if (messageItem.isMine) R.drawable.stream_tail_outgoing else R.drawable.stream_tail_incoming
                )
            )

            DrawableCompat.setTint(binding.ivTail.drawable, style.reactionViewBgColor)
        } else {
            val drawable = style.reactionViewBgDrawable
            binding.reactionsRecyclerView.background = ContextCompat.getDrawable(context, drawable)
            binding.ivTail.visibility = View.GONE
        }
    }

    private fun configParamsReactionSpace(messageItem: MessageItem) {
        if (binding.ivTail.visibility != View.VISIBLE) {
            return
        }

        binding.root.updateConstraints {
            clear(R.id.spaceReactionTail, ConstraintSet.START)
            clear(R.id.spaceReactionTail, ConstraintSet.END)
        }

        val params = binding.spaceReactionTail.layoutParams as ConstraintLayout.LayoutParams
        val activeContentViewResId = getActiveContentViewResId(messageItem.message, binding)
        if (messageItem.isMine) {
            params.endToStart = activeContentViewResId
        } else {
            params.startToEnd = activeContentViewResId
        }

        binding.spaceReactionTail.layoutParams = params
        binding.reactionsRecyclerView.post {
            params.width = binding.reactionsRecyclerView.height / 3
            binding.spaceReactionTail.layoutParams = params
        }
    }

    private fun configParamsReactionTail(messageItem: MessageItem) {
        if (binding.ivTail.visibility != View.VISIBLE) {
            return
        }

        binding.root.updateConstraints {
            clear(R.id.ivTail, ConstraintSet.START)
            clear(R.id.ivTail, ConstraintSet.END)
        }

        val params = binding.ivTail.layoutParams as ConstraintLayout.LayoutParams
        if (messageItem.isMine) {
            params.startToStart = binding.spaceReactionTail.id
        } else {
            params.endToEnd = binding.spaceReactionTail.id
        }
        binding.reactionsRecyclerView.post {
            params.height = binding.reactionsRecyclerView.height
            params.width = binding.reactionsRecyclerView.height
            params.topMargin = binding.reactionsRecyclerView.height / 3
            binding.ivTail.layoutParams = params
        }
    }

    private fun configParamsReactionRecyclerView(messageItem: MessageItem) {
        if (binding.reactionsRecyclerView.visibility != View.VISIBLE) {
            return
        }

        binding.reactionsRecyclerView.visibility = View.INVISIBLE
        binding.ivTail.visibility = View.INVISIBLE
        binding.reactionsRecyclerView.post {
            if (binding.reactionsRecyclerView.visibility == View.GONE) {
                return@post
            }

            binding.root.updateConstraints {
                clear(R.id.reactionsRecyclerView, ConstraintSet.START)
                clear(R.id.reactionsRecyclerView, ConstraintSet.END)
            }

            binding.reactionsRecyclerView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (messageItem.message.attachments.isNotEmpty()) {
                    if (messageItem.isMine) {
                        startToStart = R.id.spaceReactionTail
                    } else {
                        endToEnd = R.id.spaceReactionTail
                    }
                } else {
                    val reactionMargin =
                        context.resources.getDimensionPixelSize(R.dimen.stream_reaction_margin)
                    if (binding.tvText.width + reactionMargin < binding.reactionsRecyclerView.width) {
                        if (messageItem.isMine) {
                            endToEnd = R.id.tvText
                        } else {
                            startToStart = R.id.tvText
                        }
                    } else {
                        if (messageItem.isMine) {
                            startToStart = R.id.spaceReactionTail
                        } else {
                            endToEnd = R.id.spaceReactionTail
                        }
                    }
                }
            }

            binding.reactionsRecyclerView.isVisible = true
            binding.ivTail.isVisible = true
            configParamsReadIndicator(messageItem)
        }
    }
}
