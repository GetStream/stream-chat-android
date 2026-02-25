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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isGiphy
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.TranslatedLabel
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

/**
 * A composable function that displays a label indicating that a message has been translated.
 *
 * This label is primarily shown next to messages in channels when the message's original language
 * is different from the current user's selected language, and the auto-translation feature is enabled.
 *
 * @param messageItem The state of the message item which contains details about the message,
 * the original language, and the user's current language.
 * @param onToggleOriginalText Called when the user taps on the "Show Original" or "Show Translation" label.
 */
@Composable
public fun MessageTranslatedLabel(
    messageItem: MessageItemState,
    onToggleOriginalText: () -> Unit = {},
) {
    if (!ChatTheme.config.translation.enabled) {
        return
    }
    val userLanguage = messageItem.currentUser?.language.orEmpty()
    val i18nLanguage = messageItem.message.originalLanguage
    val isGiphy = messageItem.message.isGiphy()
    val isDeleted = messageItem.message.isDeleted()
    val translatedText = messageItem.message.getTranslation(userLanguage).ifEmpty { messageItem.message.text }
    if (!isGiphy && !isDeleted && userLanguage != i18nLanguage && translatedText != messageItem.message.text) {
        if (ChatTheme.config.translation.showOriginalEnabled) {
            // Toggle-able label to show original text or translated text
            ToggleableTranslatedLabel(
                messageItem = messageItem,
                translatedTo = userLanguage,
                onToggleOriginalText = onToggleOriginalText,
            )
        } else {
            // Always show the 'translated' label
            TranslatedLabel(translatedTo = userLanguage)
        }
    }
}

@Composable
internal fun ToggleableTranslatedLabel(
    messageItem: MessageItemState,
    translatedTo: String,
    onToggleOriginalText: () -> Unit,
) {
    if (messageItem.showOriginalText) {
        ShowTranslationLabel(onToggleOriginalText = onToggleOriginalText)
    } else {
        Row {
            TranslatedLabel(translatedTo)
            ShowOriginalLabel(onToggleOriginalText = onToggleOriginalText)
        }
    }
}

@Composable
private fun ShowTranslationLabel(onToggleOriginalText: () -> Unit) {
    Text(
        modifier = Modifier.clickable { onToggleOriginalText() },
        text = stringResource(R.string.stream_compose_message_list_show_translation),
        style = ChatTheme.typography.metadataDefault,
        color = ChatTheme.colors.textSecondary,
    )
}

@Composable
private fun ShowOriginalLabel(onToggleOriginalText: () -> Unit) {
    Text(
        modifier = Modifier.clickable(
            interactionSource = null,
            indication = null,
            onClick = onToggleOriginalText,
        ),
        text = stringResource(R.string.stream_compose_message_list_show_original),
        style = ChatTheme.typography.metadataDefault,
        color = ChatTheme.colors.textSecondary,
    )
}
