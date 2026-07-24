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
import io.getstream.chat.android.compose.pages.ChannelListPage
import io.getstream.chat.android.compose.pages.LoginPage
import io.getstream.chat.android.compose.pages.MessageListPage
import io.getstream.chat.android.compose.pages.MessageListPage.AttachmentPicker
import io.getstream.chat.android.compose.pages.MessageListPage.Composer
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message
import io.getstream.chat.android.compose.pages.MessageListPage.MessageList.Message.ContextMenu
import io.getstream.chat.android.compose.pages.ThreadPage
import io.getstream.chat.android.e2e.test.mockserver.AttachmentType
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot
import io.getstream.chat.android.e2e.test.uiautomator.defaultTimeout
import io.getstream.chat.android.e2e.test.uiautomator.device
import io.getstream.chat.android.e2e.test.uiautomator.findObjects
import io.getstream.chat.android.e2e.test.uiautomator.isDisplayed
import io.getstream.chat.android.e2e.test.uiautomator.longPress
import io.getstream.chat.android.e2e.test.uiautomator.swipeDown
import io.getstream.chat.android.e2e.test.uiautomator.swipeUp
import io.getstream.chat.android.e2e.test.uiautomator.typeText
import io.getstream.chat.android.e2e.test.uiautomator.wait
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

    fun logout(): UserRobot {
        ChannelListPage.Header.userAvatar.waitToAppearAndClick()
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
        ChannelListPage.ChannelList.channels.wait().findObjects()[channelCellIndex].click()
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
        val endTime = System.currentTimeMillis() + defaultTimeout
        while (System.currentTimeMillis() < endTime) {
            Composer.sendButton.findObjects().firstOrNull()?.let {
                it.click()
                return this
            }
            Composer.saveButton.findObjects().firstOrNull()?.let {
                it.click()
                return this
            }
            Thread.sleep(50)
        }
        error("Neither the send nor the save composer button appeared within ${defaultTimeout}ms")
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

    fun clearComposer(): UserRobot {
        Composer.inputField.waitToAppear().clear()
        return this
    }

    fun addReaction(type: ReactionType, messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
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

    fun tapOnMessage(messageCellIndex: Int = 0): UserRobot {
        MessageList.messages.waitToAppearBottomUp(withIndex = messageCellIndex).click()
        return this
    }

    fun tapOnQuotedMessage(messageCellIndex: Int = 0): UserRobot {
        Message.quotedMessage.waitToAppearAndClick()
        return this
    }

    fun tapOnScrollToBottomButton(): UserRobot {
        MessageList.scrollToBottomButton.waitToAppearAndClick()
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
