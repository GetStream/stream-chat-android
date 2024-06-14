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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl

import android.content.res.ColorStateList
import android.view.ViewGroup
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.common.state.messages.list.CancelGiphy
import io.getstream.chat.android.ui.common.state.messages.list.SendGiphy
import io.getstream.chat.android.ui.common.state.messages.list.ShuffleGiphy
import io.getstream.chat.android.ui.common.utils.GiphyInfoType
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.common.utils.giphyInfo
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageGiphyBinding
import io.getstream.chat.android.ui.feature.messages.list.GiphyViewHolderStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListeners
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.load

public class GiphyViewHolder internal constructor(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListeners?,
    public val style: GiphyViewHolderStyle,
    public val binding: StreamUiItemMessageGiphyBinding = StreamUiItemMessageGiphyBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false,
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            listeners?.let { container ->
                cancelButton.setOnClickListener {
                    container.giphySendListener.onGiphySend(CancelGiphy(data.message))
                }
                shuffleButton.setOnClickListener {
                    container.giphySendListener.onGiphySend(ShuffleGiphy(data.message))
                }
                sendButton.setOnClickListener {
                    container.giphySendListener.onGiphySend(SendGiphy(data.message))
                }
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff) {
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
                    },
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

            giphyLabelTextView.setTextStyle(style.labelTextStyle)
            giphyQueryTextView.setTextStyle(style.queryTextStyle)
            cancelButton.setTextStyle(style.cancelButtonTextStyle)
            shuffleButton.setTextStyle(style.shuffleButtonTextStyle)
            sendButton.setTextStyle(style.sendButtonTextStyle)
        }
    }

    private companion object {
        private const val GIPHY_PREFIX = "/giphy "
    }
}
