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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * Card-style Stream modal bottom sheet.
 *
 * Bakes the design-system tokens for a card sitting above the app:
 * 32dp top corners, elevated surface color, heavier scrim, and the M3 default drag handle
 * (hidden from the accessibility tree).
 * Use for menus and option pickers that appear on top of the underlying screen
 * (e.g. reactions, channel info member modal, attachment command picker).
 *
 * Preview note: when rendering this sheet under `@Preview`, wrap the call in
 * `Box(modifier = Modifier.fillMaxSize())` so the underlying Dialog has bounds to compute sheet
 * anchors against; otherwise the sheet stays blank. Android Studio's preview pane also needs a
 * manual refresh after switching tabs — a known tooling bug with Dialog-hosted previews
 * (issuetracker.google.com/issues/286371387). Paparazzi captures a single frame, so the default
 * [sheetState] swaps to a pre-expanded [SheetState] under [LocalInspectionMode] to skip M3's
 * internal show animation, which Paparazzi doesn't tick.
 *
 * @param onDismissRequest Invoked when the user dismisses the sheet.
 * @param modifier Modifier applied to the sheet container.
 * @param sheetState State controlling the sheet's visibility and target value.
 * @param content Sheet body, laid out vertically in a [ColumnScope].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StreamCardBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberStreamSheetState(initialValueInInspection = SheetValue.PartiallyExpanded),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = StreamCardSheetShape,
        containerColor = ChatTheme.colors.backgroundCoreElevation1,
        scrimColor = ChatTheme.colors.backgroundCoreScrim,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                modifier = Modifier.semantics { hideFromAccessibility() },
            )
        },
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
 *
 * Preview note: when rendering this sheet under `@Preview`, wrap the call in
 * `Box(modifier = Modifier.fillMaxSize())` so the underlying Dialog has bounds to compute sheet
 * anchors against; otherwise the sheet stays blank. Android Studio's preview pane also needs a
 * manual refresh after switching tabs — a known tooling bug with Dialog-hosted previews
 * (issuetracker.google.com/issues/286371387). Paparazzi captures a single frame, so the default
 * [sheetState] swaps to a pre-expanded [SheetState] under [LocalInspectionMode] to skip M3's
 * internal show animation, which Paparazzi doesn't tick.
 *
 * @param onDismissRequest Invoked when the user dismisses the sheet.
 * @param modifier Modifier applied to the sheet container.
 * @param sheetState State controlling the sheet's visibility and target value.
 * @param content Sheet body, laid out vertically in a [ColumnScope].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StreamScreenBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberStreamSheetState(
        initialValueInInspection = SheetValue.Expanded,
        skipPartiallyExpanded = true,
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
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

/**
 * Returns a [SheetState] suited for both production and Paparazzi.
 *
 * In production, returns [rememberModalBottomSheetState] (initial value `Hidden`) so the sheet
 * animates open via Material 3's internal show side effect.
 *
 * Under [LocalInspectionMode] (Paparazzi snapshots), constructs a [SheetState] directly with the
 * same [skipPartiallyExpanded] configuration as production, but pre-set to [initialValueInInspection]
 * so the sheet is visible from frame zero. Paparazzi captures a single frame and doesn't advance
 * M3's internal show LaunchedEffect, so a `Hidden`-initial sheet snapshots as blank. See
 * https://issuetracker.google.com/issues/283843380 and
 * https://saurabharora.dev/posts/curious-case-of-missing-bottom-sheet-previews/.
 *
 * Android Studio's preview pane runs M3's animation to its target value on its own, so this swap
 * has no observable effect there — it's exclusively a Paparazzi enabler.
 *
 * @param initialValueInInspection The sheet value to start at under [LocalInspectionMode].
 * @param skipPartiallyExpanded Whether to skip the [SheetValue.PartiallyExpanded] anchor. Must
 * match production's configuration to keep the snapshot visually faithful — screen-style sheets
 * pass `true` to avoid settling at half-height when content is tall.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberStreamSheetState(
    initialValueInInspection: SheetValue,
    skipPartiallyExpanded: Boolean = false,
): SheetState {
    if (!LocalInspectionMode.current) {
        return rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
    }
    val density = LocalDensity.current
    val confirmValueChange: (SheetValue) -> Boolean = { true }
    return rememberSaveable(
        skipPartiallyExpanded,
        saver = SheetState.Saver(
            skipPartiallyExpanded = skipPartiallyExpanded,
            confirmValueChange = confirmValueChange,
            density = density,
            skipHiddenState = true,
        ),
    ) {
        SheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
            density = density,
            initialValue = initialValueInInspection,
            confirmValueChange = confirmValueChange,
            skipHiddenState = true,
        )
    }
}

private val StreamCardSheetShape = RoundedCornerShape(
    topStart = StreamTokens.radius4xl,
    topEnd = StreamTokens.radius4xl,
    bottomStart = CornerSize(0),
    bottomEnd = CornerSize(0),
)
