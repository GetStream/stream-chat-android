package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageGiphyBinding
import io.getstream.chat.android.ui.message.list.GiphyViewHolderStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.utils.GiphyInfoType
import io.getstream.chat.android.ui.utils.giphyInfo

internal class GiphyViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer?,
    private val style: GiphyViewHolderStyle,
    internal val binding: StreamUiItemMessageGiphyBinding = StreamUiItemMessageGiphyBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            listeners?.let { container ->
                cancelButton.setOnClickListener {
                    container.giphySendListener.onGiphySend(data.message, GiphyAction.CANCEL)
                }
                shuffleButton.setOnClickListener {
                    container.giphySendListener.onGiphySend(data.message, GiphyAction.SHUFFLE)
                }
                sendButton.setOnClickListener {
                    container.giphySendListener.onGiphySend(data.message, GiphyAction.SEND)
                }
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        applyStyle()

        data.message
            .attachments
            .firstOrNull()
            ?.let {
                val url = it.giphyInfo(GiphyInfoType.FIXED_HEIGHT)?.url ?: it.let {
                    it.imagePreviewUrl ?: it.titleLink ?: it.ogUrl
                } ?: return

                binding.giphyPreview.load(
                    data = url,
                    onStart = {
                        binding.loadingProgressBar.isVisible = true
                    },
                    onComplete = {
                        binding.loadingProgressBar.isVisible = false
                    }
                )
            }

        binding.giphyQueryTextView.text = data.message
            .text
            .replace(GIPHY_PREFIX, "")
    }

    private fun applyStyle() {
        binding.apply {
            cardView.backgroundTintList = ColorStateList.valueOf(style.cardBackgroundColor)
            cardView.elevation = style.cardElevation

            horizontalDivider.setBackgroundColor(style.cardButtonDividerColor)
            verticalDivider1.setBackgroundColor(style.cardButtonDividerColor)
            verticalDivider2.setBackgroundColor(style.cardButtonDividerColor)

            giphyIconImageView.setImageDrawable(style.giphyIcon)

            style.labelTextStyle.apply(giphyLabelTextView)
            style.queryTextStyle.apply(giphyQueryTextView)
            style.cancelButtonTextStyle.apply(cancelButton)
            style.shuffleButtonTextStyle.apply(shuffleButton)
            style.sendButtonTextStyle.apply(sendButton)
        }
    }

    private companion object {
        private const val GIPHY_PREFIX = "/giphy "
    }
}
