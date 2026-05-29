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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * Card-style Stream modal bottom sheet.
 *
 * Bakes the design-system tokens for a card sitting above the app:
 * 32dp top corners, elevated surface color, heavier scrim, and the M3 default drag handle.
 * Use for menus and option pickers that appear on top of the underlying screen
 * (e.g. reactions, channel info member modal, attachment command picker).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StreamCardBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    AutoShowInInspectionMode(sheetState)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = StreamCardSheetShape,
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
        scrimColor = ChatTheme.colors.backgroundCoreScrim,
        content = content,
    )
}

/**
 * Screen-style Stream modal bottom sheet.
 *
 * Bakes the design-system tokens for a sheet that takes over the screen:
 * full-width, rectangular shape, no drag handle, app-background container,
 * and the standard scrim (mostly hidden behind the sheet).
 * Use for full-takeover surfaces such as poll results, media preview, and option votes lists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StreamScreenBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    AutoShowInInspectionMode(sheetState)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = Dp.Unspecified,
        shape = RectangleShape,
        containerColor = ChatTheme.colors.backgroundCoreApp,
        scrimColor = ChatTheme.colors.backgroundCoreScrim,
        dragHandle = null,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoShowInInspectionMode(sheetState: SheetState) {
    val inspectionMode = LocalInspectionMode.current
    LaunchedEffect(inspectionMode) {
        if (inspectionMode) sheetState.show()
    }
}

private val StreamCardSheetShape = RoundedCornerShape(
    topStart = StreamTokens.radius4xl,
    topEnd = StreamTokens.radius4xl,
    bottomStart = CornerSize(0),
    bottomEnd = CornerSize(0),
)
