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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl

import android.view.ViewGroup
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.databinding.StreamUiItemDateDividerBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Represents the date divider holder.
 */
public class DateDividerViewHolder internal constructor(
    parent: ViewGroup,
    decorators: List<Decorator>,
    public val style: MessageListItemStyle,
    public val binding: StreamUiItemDateDividerBinding = StreamUiItemDateDividerBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false,
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.DateSeparatorItem>(binding.root, decorators) {

    override fun bindData(data: MessageListItem.DateSeparatorItem, diff: MessageListItemPayloadDiff) {
        super.bindData(data, diff)

        binding.dateLabel.text = ChatUI.dateFormatter.formatRelativeDate(data.date)
        binding.dateLabel.setTextStyle(style.textStyleDateSeparator)
        binding.dateLabel.background = ShapeAppearanceModel.Builder().setAllCornerSizes(DEFAULT_CORNER_RADIUS).build()
            .let(::MaterialShapeDrawable).apply { setTint(style.dateSeparatorBackgroundColor) }
    }

    private companion object {
        private val DEFAULT_CORNER_RADIUS = 16.dpToPxPrecise()
    }
}
