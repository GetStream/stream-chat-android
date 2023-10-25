/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.getTranslation
import io.getstream.chat.android.client.models.originalLanguage
import io.getstream.chat.android.compose.state.messages.list.MessageItemState
import io.getstream.chat.android.compose.ui.components.TranslatedLabel
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isDeleted
import io.getstream.chat.android.compose.ui.util.isGiphy

/**
 * A composable function that displays a label indicating that a message has been translated.
 *
 * This label is primarily shown next to messages in channels when the message's original language
 * is different from the current user's selected language, and the auto-translation feature is enabled.
 *
 * @param messageItem The state of the message item which contains details about the message,
 * the original language, and the user's current language.
 */
@Composable
public fun MessageTranslatedLabel(
    messageItem: MessageItemState,
) {
    if (!ChatTheme.autoTranslationEnabled) {
        return
    }
    val userLanguage = messageItem.currentUser?.language.orEmpty()
    val i18nLanguage = messageItem.message.originalLanguage
    val isGiphy = messageItem.message.isGiphy()
    val isDeleted = messageItem.message.isDeleted()
    val translatedText = messageItem.message.getTranslation(userLanguage).ifEmpty { messageItem.message.text }
    if (!isGiphy && !isDeleted && userLanguage != i18nLanguage && translatedText != messageItem.message.text) {
        Spacer(modifier = Modifier.width(4.dp))
        TranslatedLabel(translatedTo = userLanguage)
    }
}
