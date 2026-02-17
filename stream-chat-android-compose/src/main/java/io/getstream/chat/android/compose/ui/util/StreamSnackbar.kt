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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.button.StreamButtonSize
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * Stream-styled snackbar that renders a message and an optional action button.
 *
 * @param snackbarData The [SnackbarData] driving the message and optional action.
 * @param modifier Modifier applied to the inner [Surface] (e.g. to control width or padding).
 */
@Composable
internal fun StreamSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val actionLabel = snackbarData.visuals.actionLabel
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(StreamTokens.spacingMd),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = modifier.shadow(4.dp, shape = SnackbarShape),
            shape = SnackbarShape,
            color = ChatTheme.colors.backgroundCoreInverse,
            contentColor = ChatTheme.colors.textOnAccent,
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = StreamTokens.spacingXs,
                    vertical = StreamTokens.spacing2xs,
                ),
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(
                            horizontal = StreamTokens.spacingXs,
                            vertical = StreamTokens.spacingSm,
                        ),
                    text = snackbarData.visuals.message,
                    style = ChatTheme.typography.bodyDefault,
                )
                if (actionLabel != null) {
                    StreamTextButton(
                        onClick = snackbarData::performAction,
                        style = StreamButtonStyleDefaults.secondaryOutline.copy(
                            contentColor = ChatTheme.colors.textOnAccent,
                        ),
                        size = StreamButtonSize.Small,
                        text = actionLabel,
                    )
                }
            }
        }
    }
}

private val SnackbarShape = RoundedCornerShape(StreamTokens.radius3xl)

/**
 * A [SnackbarHost] that defaults to [StreamSnackbar] for consistent Stream-styled snackbars.
 *
 * @param hostState The state used to show and dismiss snackbars.
 * @param modifier Modifier applied to the host layout.
 * @param snackbar The snackbar composable; defaults to [StreamSnackbar].
 */
@Composable
internal fun StreamSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { StreamSnackbar(it) },
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = snackbar,
    )
}

/** Minimal [SnackbarData] used by previews and snapshot tests. */
private class PreviewSnackbarData(
    override val visuals: SnackbarVisuals,
) : SnackbarData {
    override fun performAction() = Unit
    override fun dismiss() = Unit
}

private class PreviewSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

@Preview(showBackground = true)
@Composable
private fun StreamSnackbarMessageOnlyPreview() {
    ChatPreviewTheme {
        StreamSnackbarMessageOnly()
    }
}

@Composable
internal fun StreamSnackbarMessageOnly() {
    StreamSnackbar(
        snackbarData = PreviewSnackbarData(
            visuals = PreviewSnackbarVisuals(message = "This is a snackbar message"),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun StreamSnackbarWithActionPreview() {
    ChatPreviewTheme {
        StreamSnackbarWithAction()
    }
}

@Composable
internal fun StreamSnackbarWithAction() {
    StreamSnackbar(
        snackbarData = PreviewSnackbarData(
            visuals = PreviewSnackbarVisuals(
                message = "Something went wrong",
                actionLabel = "Retry",
            ),
        ),
    )
}
