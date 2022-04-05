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
 
package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import com.getstream.sdk.chat.utils.extensions.isNotBottomPosition
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.getCreatedAtOrNull
import io.getstream.chat.android.ui.common.extensions.getUpdatedAtOrNull
import io.getstream.chat.android.ui.common.extensions.internal.setStartDrawable
import io.getstream.chat.android.ui.common.extensions.isDeleted
import io.getstream.chat.android.ui.common.extensions.isEphemeral
import io.getstream.chat.android.ui.common.extensions.isGiphyNotEphemeral
import io.getstream.chat.android.ui.message.list.DeletedMessageListItemPredicate.VisibleToAuthorOnly
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FootnoteView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder

internal class FootnoteDecorator(
    private val dateFormatter: DateFormatter,
    private val isDirectMessage: () -> Boolean,
    private val listViewStyle: MessageListViewStyle,
    private val deletedMessageListItemPredicate: MessageListView.MessageListItemPredicate,
) : BaseDecorator() {

    /**
     * Decorates the footnote of the message containing custom attachments.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateCustomAttachmentsMessage(
        viewHolder: CustomAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.messageContainer,
        data,
    )

    /**
     * Decorates the footnote of the Giphy attachment.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyAttachmentMessage(
        viewHolder: GiphyAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupFootnote(
            viewHolder.binding.footnote,
            viewHolder.binding.root,
            viewHolder.binding.threadGuideline,
            viewHolder.binding.messageContainer,
            data,
        )
    }

    /**
     * Decorates the footnote of the message containing file attachments.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateFileAttachmentsMessage(
        viewHolder: FileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupFootnote(
            viewHolder.binding.footnote,
            viewHolder.binding.root,
            viewHolder.binding.threadGuideline,
            viewHolder.binding.messageContainer,
            data,
        )
    }

    /**
     * Decorates the footnote of the message containing image attachments.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateImageAttachmentsMessage(
        viewHolder: ImageAttachmentViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.messageContainer,
        data,
    )

    /**
     * Decorates the footnote of the plain text message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.messageContainer,
        data,
    )

    /**
     * Decorates the footnote of the ephemeral Giphy message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupSimpleFootnoteWithRootConstraints(
            viewHolder.binding.footnote,
            viewHolder.binding.root,
            viewHolder.binding.cardView,
            data
        )
        with(viewHolder.binding.footnote) {
            applyGravity(data.isMine)
            hideStatusIndicator()
        }
    }

    /**
     * Decorates the footnote of the link attachment message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateLinkAttachmentsMessage(
        viewHolder: LinkAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = setupFootnote(
        viewHolder.binding.footnote,
        viewHolder.binding.root,
        viewHolder.binding.threadGuideline,
        viewHolder.binding.messageContainer,
        data,
    )

    /**
     * Decorates the footnote of the deleted message.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        setupSimpleFootnote(viewHolder.binding.footnote, data)
    }

    private fun setupFootnote(
        footnoteView: FootnoteView,
        root: ConstraintLayout,
        threadGuideline: View,
        anchorView: View,
        data: MessageListItem.MessageItem,
    ) {
        val isSimpleFootnoteMode = data.message.replyCount == 0 || data.isThreadMode
        if (isSimpleFootnoteMode) {
            setupSimpleFootnoteWithRootConstraints(footnoteView, root, anchorView, data)
        } else {
            setupThreadFootnote(footnoteView, root, threadGuideline, data)
        }
        footnoteView.applyGravity(data.isMine)
    }

    private fun setupSimpleFootnoteWithRootConstraints(
        footnoteView: FootnoteView,
        root: ConstraintLayout,
        anchorView: View,
        data: MessageListItem.MessageItem,
    ) {
        root.updateConstraints {
            clear(footnoteView.id, ConstraintSet.TOP)
            connect(footnoteView.id, ConstraintSet.TOP, anchorView.id, ConstraintSet.BOTTOM)
        }
        setupSimpleFootnote(footnoteView, data)
    }

    private fun setupSimpleFootnote(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        footnoteView.showSimpleFootnote()
        setupMessageFooterLabel(footnoteView.footerTextLabel, data, listViewStyle.itemStyle)
        setupMessageFooterTime(footnoteView, data)
        setupDeliveryStateIndicator(footnoteView, data)
    }

    private fun setupThreadFootnote(
        footnoteView: FootnoteView,
        root: ConstraintLayout,
        threadGuideline: View,
        data: MessageListItem.MessageItem,
    ) {
        if (!listViewStyle.threadsEnabled) {
            return
        }
        root.updateConstraints {
            clear(footnoteView.id, ConstraintSet.TOP)
            connect(footnoteView.id, ConstraintSet.TOP, threadGuideline.id, ConstraintSet.BOTTOM)
        }
        footnoteView.showThreadRepliesFootnote(
            data.isMine,
            data.message.replyCount,
            data.message.threadParticipants,
            listViewStyle.itemStyle
        )
    }

    private fun setupMessageFooterLabel(
        textView: TextView,
        data: MessageListItem.MessageItem,
        style: MessageListItemStyle,
    ) {
        when {
            data.isBottomPosition() && !isDirectMessage() && data.isTheirs -> {
                textView.text = data.message.user.name
                textView.isVisible = true
                style.textStyleUserName.apply(textView)
            }

            data.isBottomPosition() &&
                data.message.isDeleted() &&
                deletedMessageListItemPredicate == VisibleToAuthorOnly -> {
                showOnlyVisibleToYou(textView, style)
            }

            data.isBottomPosition() && data.message.isEphemeral() -> {
                showOnlyVisibleToYou(textView, style)
            }

            else -> {
                textView.isVisible = false
            }
        }
    }

    /**
     * Shows the "Only visible to you" message.
     *
     * @param textView Where the message is displayed.
     * @param style [MessageListItemStyle] The style of the message. The left icon style is defined there.
     */
    private fun showOnlyVisibleToYou(textView: TextView, style: MessageListItemStyle) {
        textView.apply {
            isVisible = true
            text = context.getString(R.string.stream_ui_message_list_ephemeral_message)
            setStartDrawable(style.iconOnlyVisibleToYou)
            compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.stream_ui_spacing_small)
        }
    }

    private fun setupMessageFooterTime(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        val createdAt = data.message.getCreatedAtOrNull()
        val updatedAt = data.message.getUpdatedAtOrNull()

        when {
            data.isNotBottomPosition() || createdAt == null -> footnoteView.hideTimeLabel()
            data.message.isGiphyNotEphemeral() && updatedAt != null -> footnoteView.showTime(
                dateFormatter.formatTime(
                    updatedAt
                ),
                listViewStyle.itemStyle
            )
            else -> footnoteView.showTime(dateFormatter.formatTime(createdAt), listViewStyle.itemStyle)
        }
    }

    private fun setupDeliveryStateIndicator(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        val status = data.message.syncStatus
        when {
            !listViewStyle.itemStyle.showMessageDeliveryStatusIndicator -> Unit
            data.isNotBottomPosition() -> footnoteView.hideStatusIndicator()
            data.isTheirs -> footnoteView.hideStatusIndicator()
            data.message.isEphemeral() -> footnoteView.hideStatusIndicator()
            data.message.isDeleted() -> footnoteView.hideStatusIndicator()
            else -> when (status) {
                SyncStatus.FAILED_PERMANENTLY -> footnoteView.hideStatusIndicator()
                SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED, SyncStatus.AWAITING_ATTACHMENTS -> footnoteView.showStatusIndicator(
                    listViewStyle.itemStyle.iconIndicatorPendingSync
                )
                SyncStatus.COMPLETED -> {
                    if (data.isMessageRead) footnoteView.showStatusIndicator(listViewStyle.itemStyle.iconIndicatorRead)
                    else footnoteView.showStatusIndicator(listViewStyle.itemStyle.iconIndicatorSent)
                }
            }
        }
    }
}
