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

package io.getstream.chat.android.compose.robots

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import io.getstream.chat.android.compose.pages.ChannelInfoPage
import io.getstream.chat.android.compose.pages.ChannelListPage
import io.getstream.chat.android.compose.pages.CreatePollPage
import io.getstream.chat.android.compose.pages.LoginPage
import io.getstream.chat.android.compose.pages.MessageListPage
import io.getstream.chat.android.compose.pages.MessageListPage.AttachmentPicker
import io.getstream.chat.android.compose.pages.MessageListPage.Composer
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message.ContextMenu
import io.getstream.chat.android.compose.pages.ThreadListPage
import io.getstream.chat.android.compose.pages.ThreadPage
import io.getstream.chat.android.e2e.test.mockserver.AttachmentType
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.getstream.chat.android.e2e.test.uiautomator.defaultTimeout
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.findObjects
import io.getstream.chat.android.e2e.test.uiautomator.isDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.longPress
import io.getstream.chat.android.e2e.test.uiautomator.seconds
import io.getstream.chat.android.e2e.test.uiautomator.sleep
import io.getstream.chat.android.e2e.test.uiautomator.swipeDown
import io.getstream.chat.android.e2e.test.uiautomator.swipeUp
import io.getstream.chat.android.e2e.test.uiautomator.typeText
import io.getstream.chat.android.e2e.test.uiautomator.wait
import io.getstream.chat.android.e2e.test.uiautomator.waitDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.waitToAppear
import io.getstream.chat.android.e2e.test.uiautomator.waitToAppearAndClick
import io.getstream.chat.android.e2e.test.uiautomator.waitToAppearBottomUp
import io.getstream.chat.android.e2e.test.uiautomator.waitToDisappear

class UserRobot {

    fun sleep(timeOutMillis: Long = defaultTimeout): UserRobot {
        io.getstream.chat.android.e2e.test.uiautomator.sleep(timeOutMillis)
        return this
    }

    fun login(): UserRobot {
        LoginPage.loginButton.waitToAppearAndClick()
        return this
    }

    /** Opens the navigation drawer, which the channel list header avatar reveals. */
    fun openNavigationDrawer(): UserRobot {
        ChannelListPage.Header.userAvatar.waitToAppearAndClick()
        return this
    }

    fun openReminders(): UserRobot {
        openNavigationDrawer()
        ChannelListPage.NavigationDrawer.reminders.waitToAppearAndClick()
        return this
    }

    fun waitForChannelListToLoad(): UserRobot {
        ChannelListPage.ChannelList.channels.wait()
        return this
    }

    fun waitForMessageListToLoad(): UserRobot {
        MessageListPage.Composer.inputField.wait()
        return this
    }

    fun openChannel(channelCellIndex: Int = 0): UserRobot {
        ChannelListPage.ChannelList.channels.waitToAppearAndClick(withIndex = channelCellIndex)
        return this
    }

    fun openChannel(channelName: String): UserRobot {
        ChannelListPage.ChannelList.Channel.name(channelName).waitToAppearAndClick()
        return this
    }

    fun openContextMenu(messageCellIndex: Int = 0): UserRobot {
        val messages = MessageList.messages.waitToAppearBottomUp()
        val message = if (messages.size < messageCellIndex + 1) messages.last() else messages[messageCellIndex]
        message.longPress()
        return this
    }

    fun openContextMenu(messageText: String): UserRobot {
        val messageTextSelector = Message.text
            .text(messageText)
            .hasAncestor(MessageList.messages)
        val clickableTextSelector = Message.clickableText
            .text(messageText)
            .hasAncestor(MessageList.messages)

        val target = when {
            clickableTextSelector.isDisplayed() -> clickableTextSelector
            else -> messageTextSelector
        }

        target.waitToAppear().longPress()
        return this
    }

    fun typeText(text: String): UserRobot {
        Composer.inputField.waitToAppear().typeText(text)
        return this
    }

    fun pressBack(): UserRobot {
        device.pressBack()
        return this
    }

    fun openNotificationShade(): UserRobot {
        device.openNotification()
        return this
    }

    fun tapOnPushNotification(text: String): UserRobot {
        By.text(text).waitToAppearAndClick()
        return this
    }

    fun tapOnBackButton(): UserRobot {
        MessageListPage.Header.backButton.waitToAppearAndClick()
        return this
    }

    fun tapOnSendButton(): UserRobot {
        Composer.sendButton.waitToAppearAndClick()
        return this
    }

    /**
     * Taps whichever confirm button the composer is showing. Selecting a command suggestion
     * activates command mode, where the trailing button is the save button instead of the
     * send button; with the sample's configuration both build the same message.
     */
    private fun tapOnComposerConfirmButton(): UserRobot {
        Composer.confirmButton.waitToAppearAndClick()
        return this
    }

    fun tapOnLinkPreviewCancelButton(): UserRobot {
        Composer.linkPreviewCancelButton.waitToAppearAndClick()
        return this
    }

    fun sendMessage(text: String): UserRobot {
        typeText(text)
        tapOnSendButton()
        return this
    }

    fun deleteMessage(messageCellIndex: Int = 0, hard: Boolean = false): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.delete.waitToAppearAndClick()
        ContextMenu.ok.waitToAppearAndClick()
        return this
    }

    fun deleteMessage(text: String): UserRobot {
        openContextMenu(text)
        ContextMenu.delete.waitToAppearAndClick()
        ContextMenu.ok.waitToAppearAndClick()
        return this
    }

    fun editMessage(newText: String, messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.edit.waitToAppearAndClick()
        typeText(newText)
        Composer.saveButton.waitToAppearAndClick()
        return this
    }

    fun resendMessage(messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.resend.waitToAppearAndClick()
        return this
    }

    fun copyMessage(messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.copy.waitToAppearAndClick()
        return this
    }

    fun markMessageAsUnread(text: String): UserRobot {
        openContextMenu(text)
        ContextMenu.markAsUnread.waitToAppearAndClick()
        return this
    }

    /**
     * Opens the message menu of the message with [text], reopening it while [option] is missing.
     * The menu builds its options when it opens and keeps them while it stays open, so an option
     * that flips because of a moderation action shows up only on a later open.
     */
    internal fun openContextMenuWithOption(text: String, option: BySelector): UserRobot {
        repeat(contextMenuOpenAttempts) { attempt ->
            openContextMenu(text)
            if (option.waitDisplayed(timeOutMillis = 5.seconds)) {
                return this
            }
            if (attempt < contextMenuOpenAttempts - 1) {
                pressBack()
            }
        }
        return this
    }

    fun flagMessage(text: String): UserRobot {
        openContextMenu(text)
        ContextMenu.flag.waitToAppearAndClick()
        return this
    }

    fun confirmFlagMessage(): UserRobot {
        ContextMenu.ok.waitToAppearAndClick()
        return this
    }

    fun muteMessageAuthor(text: String): UserRobot {
        openContextMenu(text)
        ContextMenu.muteUser.waitToAppearAndClick()
        return this
    }

    fun unmuteMessageAuthor(text: String): UserRobot {
        openContextMenuWithOption(text, ContextMenu.unmuteUser)
        ContextMenu.unmuteUser.waitToAppearAndClick()
        return this
    }

    fun blockMessageAuthor(text: String): UserRobot {
        openContextMenu(text)
        ContextMenu.block.waitToAppearAndClick()
        return this
    }

    fun unblockMessageAuthor(text: String): UserRobot {
        openContextMenuWithOption(text, ContextMenu.unblock)
        ContextMenu.unblock.waitToAppearAndClick()
        return this
    }

    fun pinMessage(messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.pin.waitToAppearAndClick()
        return this
    }

    fun unpinMessage(messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.unpin.waitToAppearAndClick()
        return this
    }

    fun clearComposer(): UserRobot {
        Composer.inputField.waitToAppear().clear()
        return this
    }

    fun addReaction(type: ReactionType, messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.ReactionsView.reaction(type).waitToAppearAndClick()
        return this
    }

    fun tapOnMessageReaction(): UserRobot {
        Message.Reactions.reactions.waitToAppearAndClick()
        return this
    }

    /**
     * Adds or removes a reaction through the extended reactions picker sheet. Selecting a type
     * the user has already reacted with removes that reaction.
     */
    fun toggleReactionUsingExtendedPicker(type: ReactionType, messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.showMoreReactions.waitToAppearAndClick()
        ContextMenu.ReactionsView.reaction(type).waitToAppearAndClick()
        return this
    }

    fun deleteReaction(type: ReactionType, usingContextMenu: Boolean = true, messageCellIndex: Int = 0): UserRobot {
        if (usingContextMenu) {
            addReaction(type, messageCellIndex)
        } else {
            Message.Reactions.reactions.waitToAppearAndClick()
            Message.Reactions.reaction(type).waitToAppearAndClick()
        }
        return this
    }

    fun quoteMessage(text: String, messageCellIndex: Int = 0): UserRobot {
        selectReplyFromContextMenu(messageCellIndex)
        sendMessage(text)
        return this
    }

    fun selectReplyFromContextMenu(messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.reply.waitToAppearAndClick()
        return this
    }

    fun quoteMessage(text: String, quotedMessageText: String): UserRobot {
        openContextMenu(quotedMessageText)
        ContextMenu.reply.waitToAppearAndClick()
        sendMessage(text)
        return this
    }

    fun openThread(messageCellIndex: Int = 0, usingContextMenu: Boolean = true): UserRobot {
        if (usingContextMenu) {
            openContextMenu(messageCellIndex)
            ContextMenu.threadReply.waitToAppearAndClick()
        } else {
            Message.threadRepliesLabel.waitToAppearAndClick()
        }
        return this
    }

    fun tapOnQuotedMessage(messageCellIndex: Int = 0): UserRobot {
        // A tap that lands while the list is still moving is cancelled by the touch slop and
        // never reaches the click handler, so verify the jump moved the quote out of the
        // viewport and tap again when it did not. Does not fail on its own: the assertion
        // that follows reports a jump that never happened.
        repeat(3) {
            Message.quotedMessage.waitToAppearAndClick()
            if (!Message.quotedMessage.waitToDisappear(timeOutMillis = 3_000).isDisplayed()) {
                return this
            }
        }
        return this
    }

    fun tapOnScrollToBottomButton(): UserRobot {
        MessageList.scrollToBottomButton.waitToAppearAndClick()
        return this
    }

    fun tapOnScrollToFirstUnreadButton(): UserRobot {
        MessageList.scrollToFirstUnreadButton.waitToAppearAndClick()
        return this
    }

    fun dismissUnreadIndicator(): UserRobot {
        MessageList.scrollToFirstUnreadDismissIcon.waitToAppearAndClick()
        return this
    }

    fun sendMessageInThread(
        text: String,
        alsoSendInChannel: Boolean = false,
    ): UserRobot {
        if (alsoSendInChannel) {
            ThreadPage.ThreadList.alsoSendToChannelCheckbox.waitToAppearAndClick()
        }
        sendMessage(text)
        return this
    }

    fun quoteMessageInThread(
        text: String,
        alsoSendInChannel: Boolean = false,
        messageCellIndex: Int = 0,
    ): UserRobot {
        if (alsoSendInChannel) {
            ThreadPage.ThreadList.alsoSendToChannelCheckbox.waitToAppearAndClick()
        }
        quoteMessage(text, messageCellIndex)
        return this
    }

    fun moveToChannelListFromMessageList(): UserRobot {
        tapOnBackButton()
        waitForChannelListToLoad()
        return this
    }

    fun moveToChannelListFromThread(): UserRobot {
        tapOnBackButton()
        ThreadPage.ThreadList.alsoSendToChannelCheckbox.waitToDisappear()
        moveToChannelListFromMessageList()
        return this
    }

    fun scrollChannelListDown(times: Int = 3): UserRobot {
        device.swipeUp(times = times)
        return this
    }

    fun scrollChannelListUp(times: Int = 3): UserRobot {
        device.swipeDown(times = times)
        return this
    }

    fun scrollMessageListDown(times: Int = 3): UserRobot {
        scrollChannelListDown(times = times) // Reusing the channel list scroll
        return this
    }

    fun scrollMessageListUp(times: Int = 3): UserRobot {
        scrollChannelListUp(times = times) // Reusing the channel list scroll
        return this
    }

    fun openChannelMenu(channelCellIndex: Int = 0): UserRobot {
        ChannelListPage.ChannelList.channels.wait().findObjects()[channelCellIndex].longPress()
        return this
    }

    /** Swipes a channel item right to left to reveal the swipe actions behind it. */
    fun swipeChannel(channelCellIndex: Int = 0): UserRobot {
        val percent = 0.5f
        val channel = ChannelListPage.ChannelList.channels.wait().findObjects()[channelCellIndex]
        val rect = channel.visibleBounds
        val startX = (rect.right - (rect.width() * 0.1)).toInt()
        device.swipe(
            startX, // startX
            rect.centerY(), // startY
            (startX - (rect.width() * percent)).toInt(), // endX
            rect.centerY(), // endY
            20, // steps
        )
        return this
    }

    fun tapOnMoreSwipeAction(): UserRobot {
        ChannelListPage.ChannelList.SwipeActions.more.waitToAppearAndClick()
        return this
    }

    fun tapOnMuteSwipeAction(): UserRobot {
        ChannelListPage.ChannelList.SwipeActions.mute.waitToAppearAndClick()
        return this
    }

    fun tapOnUnmuteSwipeAction(): UserRobot {
        ChannelListPage.ChannelList.SwipeActions.unmute.waitToAppearAndClick()
        return this
    }

    fun tapOnLeaveGroup(): UserRobot {
        ChannelListPage.ChannelMenu.leaveGroup.waitToAppearAndClick()
        return this
    }

    fun tapOnDeleteGroup(): UserRobot {
        ChannelListPage.ChannelMenu.deleteGroup.waitToAppearAndClick()
        return this
    }

    fun confirmChannelAction(): UserRobot {
        ChannelListPage.ChannelMenu.confirmButton.waitToAppearAndClick()
        return this
    }

    fun tapOnViewChannelInfo(): UserRobot {
        ChannelListPage.ChannelMenu.viewInfo.waitToAppearAndClick()
        return this
    }

    fun tapOnPinnedMessagesOption(): UserRobot {
        ChannelInfoPage.pinnedMessagesOption.waitToAppearAndClick()
        return this
    }

    fun openThreadList(): UserRobot {
        ThreadListPage.threadsTab.waitToAppearAndClick()
        return this
    }

    /**
     * Taps the first thread of the thread list. The rows carry no test tag of their own, so the tap
     * lands on the parent message preview inside the row, which the row handles.
     */
    fun openThreadFromThreadList(): UserRobot {
        ThreadListPage.parentMessagePreview.waitToAppearAndClick()
        return this
    }

    fun searchForMessage(text: String): UserRobot {
        ChannelListPage.Header.searchField.waitToAppear().typeText(text)
        return this
    }

    /** Same input as [searchForMessage]; what it searches depends on the app's search mode. */
    fun searchForChannel(text: String): UserRobot {
        ChannelListPage.Header.searchField.waitToAppear().typeText(text)
        return this
    }

    /**
     * Taps the first search result. The result rows carry no test tag of their own, so the tap
     * lands on the message preview inside the row, which the row handles.
     */
    fun tapOnSearchResult(): UserRobot {
        ChannelListPage.ChannelList.Channel.messagePreview.waitToAppearAndClick()
        return this
    }

    /**
     * Scrolls the message list up one page at a time until the message with [messageText] is
     * displayed and clear of the top edge of the list, giving up after [maxScrolls] pages. The
     * first sighting is not enough: a message still clipped by the top edge after the scroll
     * settles has no laid-out text to long press, so it counts as not reached yet. Only the top
     * edge matters: scrolling up moves content downwards, so it is the edge the target enters
     * from, and a text clipped by the bottom edge is laid out and can be pressed. Does not fail
     * on its own: the interaction that follows reports the missing message.
     */
    fun scrollMessageListUpToMessage(messageText: String, maxScrolls: Int = 10): UserRobot {
        val message = Message.text
            .text(messageText)
            .hasAncestor(MessageList.messages)
        val listTop = MessageList.messageList.waitToAppear().visibleBounds.top
        repeat(maxScrolls) {
            if (message.waitDisplayed(timeOutMillis = 1_000)) {
                sleep(500) // let the fling settle before trusting the bounds
                val fullyVisible = runCatching {
                    message.findObjects().firstOrNull()?.visibleBounds?.let { it.top > listTop } == true
                }.getOrDefault(false)
                if (fullyVisible) {
                    return this
                }
            }
            scrollMessageListUp(times = 1)
        }
        return this
    }

    fun swipeMessage(messageCellIndex: Int = 0): UserRobot {
        val percent = 0.5f
        val message = MessageList.messages.waitToAppearBottomUp(withIndex = messageCellIndex)
        val rect = message.visibleBounds
        device.swipe(
            rect.left, // startX
            rect.centerY(), // startY
            (rect.right - (rect.width() * percent)).toInt(), // endX
            rect.centerY(), // endY
            20, // steps
        )
        return this
    }

    fun openComposerCommands(): UserRobot {
        // The composer redesign removed the dedicated commands button; typing '/' in the
        // input field opens the command suggestion list.
        typeText("/")
        return this
    }

    fun openAttachmentsMenu(): UserRobot {
        Composer.attachmentsButton.waitToAppearAndClick()
        return this
    }

    fun tapOnGiphyCommandSuggestion(): UserRobot {
        Composer.giphyButton.waitToAppearAndClick()
        return this
    }

    fun uploadGiphy(useComposerCommand: Boolean = false, send: Boolean = true): UserRobot {
        val giphyMessageText = "G" // any message text will result in sending a giphy
        if (useComposerCommand) {
            openComposerCommands()
            // Selecting the suggestion prefills '/giphy '; typeText replaces the whole input,
            // so the command prefix is set together with the message text.
            tapOnGiphyCommandSuggestion()
            typeText("/giphy $giphyMessageText")
            tapOnComposerConfirmButton()
        } else {
            sendMessage("/giphy $giphyMessageText")
        }

        if (send) {
            tapOnSendGiphyButton()
        }
        return this
    }

    fun quoteMessageWithGiphy(messageCellIndex: Int = 0): UserRobot {
        quoteMessage("/giphy G", messageCellIndex)
        return this
    }

    fun quoteMessageWithGiphyInThread(alsoSendInChannel: Boolean = false, messageCellIndex: Int = 0): UserRobot {
        quoteMessageInThread("/giphy G", alsoSendInChannel, messageCellIndex)
        return this
    }

    fun tapOnSendGiphyButton(): UserRobot {
        Message.GiphyButtons.send.waitToAppearAndClick()
        return this
    }

    fun tapOnShuffleGiphyButton(): UserRobot {
        Message.GiphyButtons.shuffle.waitToAppearAndClick()
        return this
    }

    fun tapOnCancelGiphyButton(): UserRobot {
        Message.GiphyButtons.cancel.waitToAppearAndClick()
        return this
    }

    fun attachFile(type: AttachmentType, multiple: Boolean = false): UserRobot {
        val count = if (multiple) 2 else 1
        Composer.attachmentsButton.waitToAppearAndClick()
        repeat(count) {
            AttachmentPicker.filesTab.waitToAppearAndClick()
            AttachmentPicker.findFilesButton.waitToAppearAndClick()

            if (!AttachmentPicker.downloadsView.isDisplayed()) {
                AttachmentPicker.rootsButton.waitToAppearAndClick()
                val documentsUiPackageName = device.currentPackageName
                By.text("Downloads")
                    .hasAncestor(By.res("$documentsUiPackageName:id/roots_list"))
                    .waitToAppearAndClick()
            }

            val attachment = if (it == 0) {
                if (type == AttachmentType.FILE) AttachmentPicker.pdf1 else AttachmentPicker.image1
            } else {
                if (type == AttachmentType.FILE) AttachmentPicker.pdf2 else AttachmentPicker.image2
            }
            attachment.waitToAppearAndClick()
        }

        return this
    }

    fun createPoll(question: String, options: List<String>): UserRobot {
        Composer.attachmentsButton.waitToAppearAndClick()
        // Selecting the polls tab auto-opens the poll creation dialog (the default
        // PollPickerMode has autoShowCreateDialog enabled), so the picker's Create Poll
        // button is never tapped.
        AttachmentPicker.pollsTab.waitToAppearAndClick()
        CreatePollPage.questionInput.waitToAppear().typeText(question)
        options.forEachIndexed { index, option ->
            CreatePollPage.addOptionButton.waitToAppearAndClick()
            CreatePollPage.optionInput.waitToAppear(withIndex = index).typeText(option)
        }
        CreatePollPage.createButton.waitToAppearAndClick()
        return this
    }

    fun castPollVote(option: String): UserRobot {
        Message.Poll.option(option).waitToAppearAndClick()
        return this
    }

    fun removePollVote(option: String): UserRobot {
        // A tap on an option the user already voted for removes the vote.
        Message.Poll.option(option).waitToAppearAndClick()
        return this
    }

    fun openPollResults(): UserRobot {
        Message.Poll.viewResultsButton.waitToAppearAndClick()
        return this
    }

    fun endPoll(): UserRobot {
        Message.Poll.endPollButton.waitToAppearAndClick()
        Message.Poll.endPollConfirmationAction.waitToAppearAndClick()
        return this
    }

    fun mentionParticipant(useSuggestions: Boolean = true, send: Boolean = true): UserRobot {
        if (useSuggestions) {
            typeText("@")
            By.text(ParticipantRobot.name).waitToAppearAndClick()
        } else {
            typeText("@${ParticipantRobot.name}")
        }

        if (send) {
            Composer.sendButton.waitToAppearAndClick()
        }
        return this
    }
}

private const val contextMenuOpenAttempts = 3
