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

package io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import io.getstream.chat.android.client.utils.message.belongsToThread
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isEphemeral
import io.getstream.chat.android.client.utils.message.isGiphy
import io.getstream.chat.android.core.utils.date.truncateFuture
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.view.internal.FootnoteView
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.BaseDecorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MediaAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessageDeletedViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.PollViewHolder
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrNull
import io.getstream.chat.android.ui.utils.extensions.isBottomPosition
import io.getstream.chat.android.ui.utils.extensions.isNotBottomPosition
import io.getstream.chat.android.ui.utils.extensions.setStartDrawable
import io.getstream.chat.android.ui.utils.extensions.updateConstraints

private const val NO_READS = 0

/**
 * Decorator for the "footnote" section of the message list items.
 *
 * @property dateFormatter [DateFormatter]. Formats the dates in the messages.
 * @property isDirectMessage Checks if the message is direct of not.
 * @property listViewStyle [MessageListViewStyle] The style of the MessageListView and its items.
 * @property deletedMessageVisibilityHandler [DeletedMessageVisibility] Used to hide or show the the deleted
 * message accordingly to the logic provided.
 * @property getLanguageDisplayName [String] Returns the language display name for the given language code.
 */
@Suppress("TooManyFunctions")
internal class FootnoteDecorator(
    private val dateFormatter: DateFormatter,
    private val isDirectMessage: () -> Boolean,
    private val isThreadEnabled: () -> Boolean,
    private val listViewStyle: MessageListViewStyle,
    private val deletedMessageVisibilityHandler: () -> DeletedMessageVisibility,
    private val getLanguageDisplayName: (code: String) -> String,
) : BaseDecorator() {

    private val itemStyle: MessageListItemStyle get() = listViewStyle.itemStyle

    /**
     * The type of the decorator. In this case [Decorator.Type.BuiltIn.FOOTNOTE].
     */
    override val type: Decorator.Type = Decorator.Type.BuiltIn.FOOTNOTE

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
     * Decorates the footnote of messages containing image and/or video attachments.
     *
     * @param viewHolder The holder to decorate.
     * @param data The item that holds all the information.
     */
    override fun decorateMediaAttachmentsMessage(
        viewHolder: MediaAttachmentsViewHolder,
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

    override fun decoratePollMessage(
        viewHolder: PollViewHolder,
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
            data,
        )
        with(viewHolder.binding.footnote) {
            applyGravity(data.isMine)
            hideStatusIndicator()
            hideReadCounter()
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
        val isThreadFootnote = data.message.belongsToThread() && !data.isThreadMode
        when (isThreadFootnote) {
            true -> setupThreadFootnote(footnoteView, root, threadGuideline, data)
            false -> setupSimpleFootnoteWithRootConstraints(footnoteView, root, anchorView, data)
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
        if (data.showMessageFooter) {
            footnoteView.showSimpleFootnote()
        } else {
            footnoteView.hideSimpleFootnote()
            return
        }
        setupMessageFooterLabel(footnoteView.footerTextLabel, data, listViewStyle.itemStyle)
        setupMessageFooterTime(footnoteView, data)
        setupMessageFooterTranslatedLabel(footnoteView, data)
        setupMessageFooterStatusIndicator(footnoteView, data)
        setupMessageFooterReadCounter(footnoteView, data)
    }

    private fun setupThreadFootnote(
        footnoteView: FootnoteView,
        root: ConstraintLayout,
        threadGuideline: View,
        data: MessageListItem.MessageItem,
    ) {
        val isThreadEnabled = listViewStyle.threadsEnabled && isThreadEnabled()
        if (!isThreadEnabled) {
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
            listViewStyle.itemStyle,
        )
        setupMessageFooterTranslatedLabel(footnoteView, data)
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
                textView.setTextStyle(style.textStyleUserName)
            }

            data.isBottomPosition() &&
                data.message.isDeleted() &&
                deletedMessageVisibilityHandler() == DeletedMessageVisibility.VISIBLE_FOR_CURRENT_USER -> {
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
        val editedAt = data.message.messageTextUpdatedAt.truncateFuture()?.let(dateFormatter::formatRelativeTime)

        when {
            createdAt == null || !data.showMessageFooter -> footnoteView.hideTimeLabel()
            else -> footnoteView.showTime(dateFormatter.formatTime(createdAt), editedAt, listViewStyle.itemStyle)
        }
    }

    private fun setupMessageFooterTranslatedLabel(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        if (!ChatUI.autoTranslationEnabled) {
            footnoteView.hideTranslatedLabel()
            return
        }
        val userLanguage = ChatUI.currentUserProvider.getCurrentUser()?.language.orEmpty()
        val i18nLanguage = data.message.originalLanguage
        val isGiphy = data.message.isGiphy()
        val isDeleted = data.message.isDeleted()
        val translatedText = data.message.getTranslation(userLanguage).ifEmpty { data.message.text }
        if (!isGiphy && !isDeleted && userLanguage != i18nLanguage && translatedText != data.message.text) {
            val languageDisplayName = getLanguageDisplayName(userLanguage)
            footnoteView.showTranslatedLabel(languageDisplayName, listViewStyle.itemStyle)
        } else {
            footnoteView.hideTranslatedLabel()
        }
    }

    private fun setupMessageFooterStatusIndicator(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        val indicatorDisabled = !listViewStyle.itemStyle.showMessageDeliveryStatusIndicator
        val shouldHideReadRelatedInfo = shouldHideReadRelatedInfo(data)
        if (indicatorDisabled || shouldHideReadRelatedInfo) {
            footnoteView.hideStatusIndicator()
            return
        }
        val statusIndicator = when (data.message.syncStatus) {
            SyncStatus.IN_PROGRESS,
            SyncStatus.SYNC_NEEDED,
            SyncStatus.AWAITING_ATTACHMENTS,
            -> itemStyle.iconIndicatorPendingSync
            SyncStatus.COMPLETED -> when (data.isMessageRead) {
                true -> itemStyle.iconIndicatorRead
                else -> itemStyle.iconIndicatorSent
            }
            else -> null
        }
        if (statusIndicator != null) {
            footnoteView.showStatusIndicator(statusIndicator)
        } else {
            footnoteView.hideStatusIndicator()
        }
    }

    private fun setupMessageFooterReadCounter(footnoteView: FootnoteView, data: MessageListItem.MessageItem) {
        val isReadCountDisabled = !listViewStyle.readCountEnabled
        val shouldHideReadRelatedInfo = shouldHideReadRelatedInfo(data)
        if (isReadCountDisabled || shouldHideReadRelatedInfo) {
            footnoteView.hideReadCounter()
            return
        }
        val readCount = when (data.message.syncStatus == SyncStatus.COMPLETED && data.isMessageRead) {
            true -> data.messageReadBy.size
            else -> NO_READS
        }
        if (readCount > 0) {
            footnoteView.showReadCounter(readCount, listViewStyle.itemStyle)
        } else {
            footnoteView.hideReadCounter()
        }
    }

    private fun shouldHideReadRelatedInfo(data: MessageListItem.MessageItem): Boolean {
        val status = data.message.syncStatus
        val isNotBottomPosition = data.isNotBottomPosition()
        val isTheirs = data.isTheirs
        val isEphemeral = data.message.isEphemeral()
        val isDeleted = data.message.isDeleted()
        val isFailedPermanently = status == SyncStatus.FAILED_PERMANENTLY

        return isNotBottomPosition || isTheirs || isEphemeral || isDeleted || isFailedPermanently
    }
}
