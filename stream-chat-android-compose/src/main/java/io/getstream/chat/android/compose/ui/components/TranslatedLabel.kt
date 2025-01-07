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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Locale

/**
 * Represents a label translated within the app. This label is primarily used for channels and messages,
 * typically adjacent to the [Timestamp].
 *
 * @param translatedTo The language code indicating the language to which the original text was translated.
 * @param modifier Modifier for styling.
 */
@Composable
public fun TranslatedLabel(
    translatedTo: String,
    modifier: Modifier = Modifier,
) {
    val textLanguageMetaInfo = if (LocalInspectionMode.current) {
        "Translated to $translatedTo"
    } else {
        val languageDisplayName = Locale(translatedTo).getDisplayName(Locale.getDefault())
        LocalContext.current.getString(R.string.stream_compose_message_list_translated, languageDisplayName)
    }

    Text(
        modifier = modifier,
        text = textLanguageMetaInfo,
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textLowEmphasis,
    )
}
