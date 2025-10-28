/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import io.getstream.chat.android.compose.ui.theme.MentionStyleFactory
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention

/**
 * A custom implementation of [MentionStyleFactory] that applies a specific color to user mentions.
 *
 * @param color The color to apply to user mentions.
 */
class CustomMentionStyleFactory(private val color: Color) : MentionStyleFactory {

    override fun styleFor(mention: Mention): SpanStyle? = when (mention) {
        is Mention.User -> SpanStyle(color = color)
        else -> null
    }
}
