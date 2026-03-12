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

package io.getstream.chat.android.compose.ui.messages.composer.internal.suggestions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

@Composable
internal fun SuggestionsMenu(
    contentMaxHeight: Dp,
    content: @Composable () -> Unit,
) {
    if (ChatTheme.config.composer.floatingStyleEnabled) {
        Popup(popupPositionProvider = AboveAnchorPositionProvider) {
            Card(
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .padding(
                        horizontal = StreamTokens.spacingMd,
                        vertical = StreamTokens.spacingSm,
                    )
                    .heightIn(max = contentMaxHeight),
                elevation = CardDefaults.cardElevation(defaultElevation = StreamTokens.elevation3),
                shape = SuggestionsShape,
                colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.backgroundElevationElevation1),
            ) {
                content()
            }
        }
    } else {
        Box(
            modifier = Modifier.heightIn(max = contentMaxHeight),
        ) {
            content()
        }
    }
}

private val SuggestionsShape = RoundedCornerShape(StreamTokens.radius3xl)

private object AboveAnchorPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ) = IntOffset(
        x = 0,
        y = anchorBounds.top - popupContentSize.height,
    )
}
