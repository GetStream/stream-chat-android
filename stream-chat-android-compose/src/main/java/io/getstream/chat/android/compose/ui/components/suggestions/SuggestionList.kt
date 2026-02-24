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

package io.getstream.chat.android.compose.ui.components.suggestions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.AboveAnchorPopupPositionProvider

/**
 * Represents the suggestion list popup that allows user to auto complete the current input.
 *
 * @param modifier Modifier for styling.
 * @param shape The shape of suggestion list popup.
 * @param contentPadding The inner content padding inside the popup.
 * @param headerContent The content shown at the top of a suggestion list popup.
 * @param centerContent The content shown inside the suggestion list popup.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun SuggestionList(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(StreamTokens.radiusXl),
    contentPadding: PaddingValues = PaddingValues(vertical = ChatTheme.dimens.suggestionListPadding),
    headerContent: @Composable () -> Unit = {},
    centerContent: @Composable () -> Unit,
) {
    Popup(popupPositionProvider = AboveAnchorPopupPositionProvider) {
        Card(
            modifier = modifier.semantics { testTagsAsResourceId = true },
            elevation = CardDefaults.cardElevation(defaultElevation = ChatTheme.dimens.suggestionListElevation),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.backgroundElevationElevation1),
        ) {
            Column(Modifier.padding(contentPadding)) {
                headerContent()

                centerContent()
            }
        }
    }
}
