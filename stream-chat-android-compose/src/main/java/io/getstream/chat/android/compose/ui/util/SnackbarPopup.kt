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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider

/**
 * A snackbar wrapped inside of a popup allowing it be displayed above the Composable it's anchored to.
 *
 * @param hostState The state of this component to read and show Snackbars accordingly
 * @param snackbar The Snackbar to be shown at the appropriate time
 * with appearance based on the SnackbarData provided as a param
 */
@Composable
internal fun SnackbarPopup(
    hostState: SnackbarHostState,
    snackbar: @Composable (SnackbarData) -> Unit = { StreamSnackbar(it) },
) {
    Popup(popupPositionProvider = AboveAnchorPopupPositionProvider) {
        StreamSnackbarHost(
            hostState = hostState,
            snackbar = snackbar,
        )
    }
}

internal object AboveAnchorPopupPositionProvider : PopupPositionProvider {
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
