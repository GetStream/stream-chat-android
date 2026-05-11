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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.button.StreamButtonSize
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * Visual styles a [StreamSnackbar] can render in. Selected via [StreamSnackbarVisuals.variant] when
 * pushing a snackbar onto a [SnackbarHostState].
 */
internal enum class StreamSnackbarVariant {
    Default,
    Error,
    Success,
    Loading,
}

/**
 * [SnackbarVisuals] carrying a [StreamSnackbarVariant]. Pass to
 * [SnackbarHostState.showSnackbar] so [StreamSnackbar] can render the matching leading element
 * (e.g. exclamation icon for [StreamSnackbarVariant.Error]).
 */
internal data class StreamSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val variant: StreamSnackbarVariant = StreamSnackbarVariant.Default,
) : SnackbarVisuals

/**
 * Stream-styled snackbar that renders a message, an optional leading element (based on the
 * snackbar's variant), and an optional action button.
 *
 * @param snackbarData The [SnackbarData] driving the message, variant, and optional action.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun StreamSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val actionLabel = snackbarData.visuals.actionLabel
    val variant = (snackbarData.visuals as? StreamSnackbarVisuals)?.variant
        ?: StreamSnackbarVariant.Default
    Box(modifier = modifier.padding(StreamTokens.spacingMd)) {
        Surface(
            modifier = Modifier.shadow(elevation = StreamTokens.elevation3, shape = SnackbarShape),
            shape = SnackbarShape,
            color = ChatTheme.colors.backgroundCoreInverse,
            contentColor = ChatTheme.colors.textOnInverse,
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = StreamTokens.spacingXs,
                    vertical = StreamTokens.spacing2xs,
                ),
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SnackbarContent(
                    modifier = Modifier.weight(1f, fill = false),
                    variant = variant,
                    message = snackbarData.visuals.message,
                )
                if (actionLabel != null) {
                    StreamTextButton(
                        onClick = snackbarData::performAction,
                        style = StreamButtonStyleDefaults.secondaryOutline.copy(
                            contentColor = ChatTheme.colors.textOnInverse,
                        ),
                        size = StreamButtonSize.Small,
                        text = actionLabel,
                    )
                }
            }
        }
    }
}

@Composable
private fun SnackbarContent(
    modifier: Modifier,
    variant: StreamSnackbarVariant,
    message: String,
) {
    Row(
        modifier = modifier.padding(
            start = if (variant == StreamSnackbarVariant.Default) {
                StreamTokens.spacingXs
            } else {
                StreamTokens.spacing2xs
            },
            end = StreamTokens.spacingXs,
        ),
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SnackbarLeadingContent(variant = variant)
        Text(
            modifier = Modifier.padding(vertical = StreamTokens.spacingSm),
            text = message,
            style = ChatTheme.typography.bodyDefault,
        )
    }
}

@Composable
private fun SnackbarLeadingContent(variant: StreamSnackbarVariant) {
    when (variant) {
        StreamSnackbarVariant.Default -> Unit
        StreamSnackbarVariant.Error -> Icon(
            painter = painterResource(id = R.drawable.stream_design_ic_exclamation_circle_fill),
            contentDescription = null,
            tint = ChatTheme.colors.textOnInverse,
        )
        StreamSnackbarVariant.Success -> Icon(
            painter = painterResource(id = R.drawable.stream_design_ic_checkmark),
            contentDescription = null,
            tint = ChatTheme.colors.textOnInverse,
        )
        StreamSnackbarVariant.Loading -> CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            trackColor = ChatTheme.colors.textOnInverse.copy(alpha = .35f),
            color = ChatTheme.colors.textOnInverse,
        )
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

@Preview(showBackground = true)
@Composable
private fun StreamSnackbarDefaultPreview() {
    ChatPreviewTheme {
        StreamSnackbarDefault()
    }
}

@Composable
internal fun StreamSnackbarDefault() {
    StreamSnackbar(
        snackbarData = PreviewSnackbarData(
            visuals = StreamSnackbarVisuals(message = "This is a snackbar message"),
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
            visuals = StreamSnackbarVisuals(
                message = "Something went wrong",
                actionLabel = "Retry",
                variant = StreamSnackbarVariant.Error,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun StreamSnackbarErrorPreview() {
    ChatPreviewTheme {
        StreamSnackbarError()
    }
}

@Composable
internal fun StreamSnackbarError() {
    StreamSnackbar(
        snackbarData = PreviewSnackbarData(
            visuals = StreamSnackbarVisuals(
                message = "Not available while editing",
                variant = StreamSnackbarVariant.Error,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun StreamSnackbarSuccessPreview() {
    ChatPreviewTheme {
        StreamSnackbarSuccess()
    }
}

@Composable
internal fun StreamSnackbarSuccess() {
    StreamSnackbar(
        snackbarData = PreviewSnackbarData(
            visuals = StreamSnackbarVisuals(
                message = "Message sent",
                variant = StreamSnackbarVariant.Success,
            ),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun StreamSnackbarLoadingPreview() {
    ChatPreviewTheme {
        StreamSnackbarLoading()
    }
}

@Composable
internal fun StreamSnackbarLoading() {
    StreamSnackbar(
        snackbarData = PreviewSnackbarData(
            visuals = StreamSnackbarVisuals(
                message = "Uploading attachment",
                variant = StreamSnackbarVariant.Loading,
            ),
        ),
    )
}
