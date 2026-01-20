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
import io.getstream.chat.android.compose.uiautomator.defaultTimeout
import io.getstream.chat.android.compose.uiautomator.device
import io.getstream.chat.android.compose.uiautomator.findObject
import io.getstream.chat.android.compose.uiautomator.findObjects
import io.getstream.chat.android.compose.uiautomator.isDisplayed
import io.getstream.chat.android.compose.uiautomator.longPress
import io.getstream.chat.android.compose.uiautomator.swipeDown
import io.getstream.chat.android.compose.uiautomator.swipeUp
import io.getstream.chat.android.compose.uiautomator.tapOnScreenCenter
import io.getstream.chat.android.compose.uiautomator.typeText
import io.getstream.chat.android.compose.uiautomator.wait
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.getstream.chat.android.compose.uiautomator.waitToDisappear
import io.getstream.chat.android.e2e.test.mockserver.AttachmentType
import io.getstream.chat.android.e2e.test.mockserver.ReactionType
import io.getstream.chat.android.e2e.test.robots.ParticipantRobot

class UserRobot {

    fun sleep(timeOutMillis: Long = defaultTimeout): UserRobot {
        io.getstream.chat.android.compose.uiautomator.sleep(timeOutMillis)
        return this
    }

    fun login(): UserRobot {
        LoginPage.loginButton.waitToAppear().click()
        return this
    }

    fun logout(): UserRobot {
        ChannelListPage.Header.userAvatar.waitToAppear().click()
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
        MessageList.messages.waitToAppear()
        val messages = MessageList.messages.findObjects()
        val message = if (messages.size < messageCellIndex + 1) messages.last() else messages[messageCellIndex]
        message.longPress()
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

    fun tapOnBackButton(): UserRobot {
        MessageListPage.Header.backButton.waitToAppear().click()
        return this
    }

    fun tapOnSendButton(): UserRobot {
        Composer.sendButton.waitToAppear().click()
        return this
    }

    fun tapOnLinkPreviewCancelButton(): UserRobot {
        Composer.linkPreviewCancelButton.waitToAppear().click()
        return this
    }

    fun sendMessage(text: String): UserRobot {
        typeText(text)
        tapOnSendButton()
        return this
    }

    fun deleteMessage(messageCellIndex: Int = 0, hard: Boolean = false): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.delete.waitToAppear().click()
        ContextMenu.ok.waitToAppear().click()
        return this
    }

    fun editMessage(newText: String, messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.edit.waitToAppear().click()
        sendMessage(newText)
        return this
    }

    fun resendMessage(messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.resend.waitToAppear().click()
        return this
    }

    fun clearComposer(): UserRobot {
        Composer.inputField.waitToAppear().clear()
        return this
    }

    fun addReaction(type: ReactionType, messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.ReactionsView.reaction(type).waitToAppear().click()
        return this
    }

    fun deleteReaction(type: ReactionType, usingContextMenu: Boolean = true, messageCellIndex: Int = 0): UserRobot {
        if (usingContextMenu) {
            addReaction(type, messageCellIndex)
        } else {
            Message.Reactions.reactions.waitToAppear().click()
            Message.Reactions.reaction(type).waitToAppear().click()
        }
        return this
    }

    fun quoteMessage(text: String, messageCellIndex: Int = 0): UserRobot {
        openContextMenu(messageCellIndex)
        ContextMenu.reply.waitToAppear().click()
        sendMessage(text)
        return this
    }

    fun openThread(messageCellIndex: Int = 0, usingContextMenu: Boolean = true): UserRobot {
        if (usingContextMenu) {
            openContextMenu(messageCellIndex)
            ContextMenu.threadReply.waitToAppear().click()
        } else {
            Message.threadRepliesLabel.waitToAppear().click()
        }
        return this
    }

    fun tapOnMessage(messageCellIndex: Int = 0): UserRobot {
        MessageList.messages.waitToAppear(withIndex = messageCellIndex).click()
        return this
    }

    fun tapOnQuotedMessage(messageCellIndex: Int = 0): UserRobot {
        Message.quotedMessage.waitToAppear().click()
        return this
    }

    fun tapOnScrollToBottomButton(): UserRobot {
        MessageList.scrollToBottomButton.waitToAppear().click()
        return this
    }

    fun sendMessageInThread(
        text: String,
        alsoSendInChannel: Boolean = false,
    ): UserRobot {
        if (alsoSendInChannel) {
            ThreadPage.ThreadList.alsoSendToChannelCheckbox.waitToAppear().click()
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
            ThreadPage.ThreadList.alsoSendToChannelCheckbox.waitToAppear().click()
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
        val message = MessageList.messages.waitToAppear(withIndex = messageCellIndex)
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
        Composer.commandsButton.waitToAppear().click()
        return this
    }

    fun openAttachmentsMenu(): UserRobot {
        Composer.attachmentsButton.waitToAppear().click()
        return this
    }

    fun uploadGiphy(useComposerCommand: Boolean = false, send: Boolean = true): UserRobot {
        val giphyMessageText = "G" // any message text will result in sending a giphy
        if (useComposerCommand) {
            openComposerCommands()
            Composer.giphyButton.waitToAppear().click()
            Composer.inputField.findObject().click()
            device.typeText(giphyMessageText)
            Composer.sendButton.findObject().click()
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
        Message.GiphyButtons.send.waitToAppear().click()
        return this
    }

    fun tapOnShuffleGiphyButton(): UserRobot {
        Message.GiphyButtons.shuffle.waitToAppear().click()
        return this
    }

    fun tapOnCancelGiphyButton(): UserRobot {
        Message.GiphyButtons.cancel.waitToAppear().click()
        return this
    }

    fun uploadAttachment(type: AttachmentType, multiple: Boolean = false, send: Boolean = true): UserRobot {
        val count = if (multiple) 2 else 1
        repeat(count) {
            Composer.attachmentsButton.waitToAppear().click()
            AttachmentPicker.filesTab.waitToAppear().click()
            AttachmentPicker.findFilesButton.waitToAppear().click()

            if (!AttachmentPicker.downloadsView.isDisplayed()) {
                AttachmentPicker.rootsButton.waitToAppear().click()
                val documentsUiPackageName = device.currentPackageName
                By.text("Downloads")
                    .hasAncestor(By.res("$documentsUiPackageName:id/roots_list"))
                    .waitToAppear()
                    .click()
            }

            if (type == AttachmentType.FILE) AttachmentPicker.pdf1 else AttachmentPicker.image1

            if (it == 0) {
                val attachment = if (type == AttachmentType.FILE) AttachmentPicker.pdf1 else AttachmentPicker.image1
                attachment.waitToAppear().click()
            } else {
                val attachment = if (type == AttachmentType.FILE) AttachmentPicker.pdf2 else AttachmentPicker.image2
                attachment.waitToAppear().click()
            }
        }

        if (send) {
            Composer.sendButton.waitToAppear().click()
        }

        return this
    }

    fun mentionParticipant(useSuggestions: Boolean = true, send: Boolean = true): UserRobot {
        if (useSuggestions) {
            typeText("@")
            By.text(ParticipantRobot.name).waitToAppear().click()
        } else {
            typeText("@${ParticipantRobot.name}")
        }

        if (send) {
            Composer.sendButton.waitToAppear().click()
        }
        return this
    }

    fun tapOnMessageList() {
        device.tapOnScreenCenter()
    }
}
