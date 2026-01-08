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

package io.getstream.chat.android.ui.feature.messages.preview

import io.getstream.chat.android.ui.feature.mentions.list.MentionListView
import io.getstream.chat.android.ui.feature.messages.preview.internal.MessagePreviewView
import io.getstream.chat.android.ui.feature.search.list.SearchResultListView
import io.getstream.chat.android.ui.font.TextStyle

/**
 * Style for [MessagePreviewView] used by [MentionListView] and [SearchResultListView].
 *
 * @property messageSenderTextStyle Appearance for message sender text.
 * @property messageTextStyle Appearance for message text.
 * @property messageTimeTextStyle Appearance for message time text.
 */
public data class MessagePreviewStyle(
    val messageSenderTextStyle: TextStyle,
    val messageTextStyle: TextStyle,
    val messageTimeTextStyle: TextStyle,
)
