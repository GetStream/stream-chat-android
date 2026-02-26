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

package io.getstream.chat.android.compose.ui.threads

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable

/**
 * Composable banner displayed at the top of the thread list. Supports three visual states:
 * - [ThreadListBannerState.UnreadThreads]: shows the count of new threads with a refresh icon.
 * - [ThreadListBannerState.Loading]: shows a spinner with "Loading..." text.
 * - [ThreadListBannerState.Error]: shows an error icon with a retry prompt.
 *
 * @param state The current [ThreadListBannerState] to render.
 * @param modifier [Modifier] instance for general styling.
 * @param onClick Action invoked when the user clicks on the banner. Not invoked for [ThreadListBannerState.Loading].
 */
@Suppress("LongMethod")
@Composable
public fun ThreadListBanner(
    state: ThreadListBannerState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isClickable = state !is ThreadListBannerState.Loading && onClick != null
    val clickableModifier = if (isClickable) {
        Modifier.clickable { onClick.invoke() }
    } else {
        Modifier
    }
    val color = ChatTheme.colors.chatTextSystem

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundCoreSurface)
            .then(clickableModifier)
            .padding(StreamTokens.spacingSm),
        horizontalArrangement = Arrangement.spacedBy(
            StreamTokens.spacingXs,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state) {
            is ThreadListBannerState.UnreadThreads -> {
                Icon(
                    modifier = Modifier.size(StreamTokens.size16),
                    painter = painterResource(R.drawable.stream_compose_ic_union),
                    contentDescription = null,
                    tint = color,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.stream_compose_thread_list_new_threads,
                        state.count,
                        state.count,
                    ),
                    style = ChatTheme.typography.metadataEmphasis,
                    color = color,
                )
            }

            is ThreadListBannerState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(StreamTokens.size16),
                    strokeWidth = 2.dp,
                    color = color,
                )
                Text(
                    text = stringResource(R.string.stream_compose_thread_list_banner_loading),
                    style = ChatTheme.typography.metadataEmphasis,
                    color = color,
                )
            }

            is ThreadListBannerState.Error -> {
                Icon(
                    modifier = Modifier.size(StreamTokens.size16),
                    painter = painterResource(R.drawable.stream_compose_ic_exclamation_circle),
                    contentDescription = null,
                    tint = color,
                )
                Text(
                    text = stringResource(R.string.stream_compose_thread_list_banner_error),
                    style = ChatTheme.typography.metadataEmphasis,
                    color = color,
                )
            }
        }
    }
}

/**
 * Sealed interface representing the possible states of the [ThreadListBanner].
 */
public sealed interface ThreadListBannerState {
    /**
     * Indicates that there are unseen threads available.
     *
     * @param count The number of unseen threads.
     */
    public data class UnreadThreads(val count: Int) : ThreadListBannerState

    /** Indicates that a refresh/reload is in progress. */
    public data object Loading : ThreadListBannerState

    /** Indicates that the last load/refresh failed. */
    public data object Error : ThreadListBannerState
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ThreadListBannerUnreadPreview() {
    ChatPreviewTheme {
        Surface {
            Column {
                ThreadListBanner(
                    state = ThreadListBannerState.UnreadThreads(count = 5),
                    onClick = {},
                )
            }
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ThreadListBannerLoadingPreview() {
    ChatPreviewTheme {
        Surface {
            ThreadListBanner(state = ThreadListBannerState.Loading)
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ThreadListBannerErrorPreview() {
    ChatPreviewTheme {
        Surface {
            ThreadListBanner(
                state = ThreadListBannerState.Error,
                onClick = {},
            )
        }
    }
}
