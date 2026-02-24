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

package io.getstream.chat.android.compose.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a reusable and generic modal menu useful for showing info about selected items.
 *
 * @param modifier Modifier for styling.
 * @param shape Changes the shape of the dialog.
 * @param overlayColor The color applied to the overlay.
 * @param onDismiss Handler called when the dialog is dismissed.
 * @param headerContent The content shown at the top of the dialog.
 * @param centerContent The content shown in the dialog.
 */
@Composable
public fun SimpleMenu(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    overlayColor: Color = ChatTheme.colors.overlayBackground,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {},
    centerContent: @Composable ColumnScope.() -> Unit = {},
) {
    Box(
        modifier = Modifier
            .background(overlayColor)
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = null,
            ),
    ) {
        Card(
            modifier = modifier
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = null,
                ),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.backgroundElevationElevation1),
        ) {
            Column {
                headerContent()

                centerContent()
            }
        }
    }

    BackHandler(enabled = true, onBack = onDismiss)
}
