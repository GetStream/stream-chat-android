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

package io.getstream.chat.ui.sample.feature.chat.messagelist.options

import android.content.Context
import androidx.core.content.ContextCompat
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.options.message.MessageOptionItem
import io.getstream.chat.android.ui.feature.messages.list.options.message.MessageOptionItemsFactory
import io.getstream.chat.ui.sample.R

class CustomMessageOptionItemsFactory(
    private val context: Context,
) : MessageOptionItemsFactory {

    override fun createMessageOptionItems(
        selectedMessage: Message,
        currentUser: User?,
        isInThread: Boolean,
        ownCapabilities: Set<String>,
        style: MessageListViewStyle,
    ): List<MessageOptionItem> {
        return listOf(
            MessageOptionItem(
                optionText = context.getString(R.string.message_details),
                optionIcon = ContextCompat.getDrawable(context, R.drawable.ic_message_details)!!,
                messageAction = CustomAction(
                    selectedMessage,
                    mapOf(
                        CustomMessageOption.TYPE to CustomMessageOption.TYPE_MESSAGE_DETAILS,
                    ),
                ),
            ),
        )
    }
}
