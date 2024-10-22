/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.compose.pages.ChannelListPage
import io.getstream.chat.android.compose.pages.LoginPage
import io.getstream.chat.android.compose.pages.MessageListPage
import io.getstream.chat.android.compose.uiautomator.findObjects
import io.getstream.chat.android.compose.uiautomator.wait
import io.getstream.chat.android.compose.uiautomator.waitToAppear
import io.getstream.chat.android.e2e.test.chat.ReactionType

class UserRobot {

    fun login() : UserRobot {
        LoginPage.loginButton.waitToAppear().click()
        return this
    }

    fun logout() : UserRobot {
        return this
    }

    fun openChannel(channelCellIndex: Int = 0) : UserRobot {
        ChannelListPage.channels.wait().findObjects()[channelCellIndex].click()
        return this
    }

    fun openContextMenu(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun typeText(text: String) : UserRobot {
        return this
    }

    fun sendMessage(text: String) : UserRobot {
        return this
    }

    fun deleteMessage(messageCellIndex: Int = 0, hard: Boolean = false) : UserRobot {
        return this
    }

    fun editMessage(newText: String, messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun clearComposer() : UserRobot {
        return this
    }

    fun addReaction(type: ReactionType, messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun deleteReaction(type: ReactionType, messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun selectOptionFromContextMenu(option: MessageListPage.ContextMenu, messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun quoteMessage(text: String, messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun openThread(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun tapOnMessage(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun tapOnQuotedMessage(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun tapOnScrollToBottomButton() : UserRobot {
        return this
    }

    fun quoteMessageInThread(text: String, alsoSendInChannel: Boolean = false, messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun tapOnBackButton() : UserRobot {
        return this
    }

    fun leaveChatFromChannelList() : UserRobot {
        return this
    }

    fun moveToChannelListFromThread() : UserRobot {
        return this
    }

    fun scrollChannelListDown(times: Int = 1) : UserRobot {
        return this
    }

    fun scrollChannelListUp(times: Int = 1) : UserRobot {
        return this
    }

    fun scrollMessageListDown(times: Int = 1) : UserRobot {
        return this
    }

    fun scrollMessageListUp(times: Int = 1) : UserRobot {
        return this
    }

    fun swipeMessage(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun openComposerCommands() : UserRobot {
        return this
    }

    fun sendGiphy(useComposerCommand: Boolean = false, send: Boolean = true) : UserRobot {
        return this
    }

    fun quoteMessageWithGiphy(useComposerCommand: Boolean = false, messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun quoteMessageWithGiphyInThread(
        useComposerCommand: Boolean = false,
        alsoSendInChannel: Boolean = false,
        messageCellIndex: Int = 0
    ) : UserRobot {
        return this
    }

    fun tapOnSendGiphyButton(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun tapOnShuffleGiphyButton(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun tapOnCancelGiphyButton(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun uploadImage(count: Int = 1, send: Boolean = true) : UserRobot {
        return this
    }

    fun restartImageUpload(messageCellIndex: Int = 0) : UserRobot {
        return this
    }

    fun mentionParticipant(manually: Boolean = false) : UserRobot {
        return this
    }
}