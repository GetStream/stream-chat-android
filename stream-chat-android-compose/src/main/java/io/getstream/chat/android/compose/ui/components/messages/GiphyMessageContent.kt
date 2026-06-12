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

package io.getstream.chat.android.compose.ui.components.messages

import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.button.StreamTextButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.GiphyAttachmentContentParams
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.list.CancelGiphy
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.SendGiphy
import io.getstream.chat.android.ui.common.state.messages.list.ShuffleGiphy
import kotlinx.coroutines.delay

/**
 * Represents the content of an ephemeral giphy message.
 *
 * @param message The ephemeral giphy message.
 * @param currentUser The current user that's logged in.
 * @param modifier Modifier for styling.
 * @param onGiphyActionClick Handler when the user clicks on action button.
 */
@Suppress("LongMethod")
@Composable
public fun GiphyMessageContent(
    message: Message,
    currentUser: User?,
    modifier: Modifier = Modifier,
    onGiphyActionClick: (GiphyAction) -> Unit = {},
) {
    val colors = ChatTheme.colors
    val view = LocalView.current
    val sentAnnouncement = stringResource(R.string.stream_compose_message_list_giphy_sent)
    val cancelledAnnouncement = stringResource(R.string.stream_compose_message_list_giphy_cancelled)
    val shuffledAnnouncement = stringResource(R.string.stream_compose_message_list_giphy_shuffled)

    val giphyPreviewLabel = stringResource(R.string.stream_compose_giphy_preview_label)

    val isTouchExplorationEnabled = rememberIsTouchExplorationEnabled()
    val previewFocusRequester = remember { FocusRequester() }
    // Track whether the preview has already requested focus so that a LazyColumn dispose +
    // re-entry (the preview scrolling out of the viewport and back) does not re-steal
    // TalkBack focus from wherever the user has moved it in the meantime.
    var hasRequestedFocus by rememberSaveable(message.id) { mutableStateOf(false) }
    LaunchedEffect(message.id, isTouchExplorationEnabled) {
        if (!isTouchExplorationEnabled || hasRequestedFocus) return@LaunchedEffect
        // Let Compose layout + the accessibility tree settle before stealing TalkBack focus,
        // otherwise our request loses to the composer's post-command focus reshuffling.
        delay(PreviewFocusRequestDelayMs)
        previewFocusRequester.requestFocus()
        hasRequestedFocus = true
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .applyIf(isTouchExplorationEnabled) {
                    focusRequester(previewFocusRequester).focusable()
                }
                // Announce the focused preview as a single TalkBack stop that leads with the
                // "Giphy preview" label. mergeDescendants prepends it to the children's natural
                // announcements (the only-visible banner, alt text, Giphy label), keeping their
                // test tags and any integrator overrides intact.
                .semantics(mergeDescendants = true) { contentDescription = giphyPreviewLabel },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
            ) {
                Icon(
                    painter = painterResource(R.drawable.stream_design_ic_eye_fill),
                    contentDescription = null,
                    tint = colors.chatTextOutgoing,
                )
                Text(
                    text = stringResource(R.string.stream_compose_only_visible_to_you),
                    style = ChatTheme.typography.captionEmphasis,
                    color = colors.chatTextOutgoing,
                )
            }

            val attachmentState = AttachmentState(
                message = message,
                isMine = message.user.id == currentUser?.id,
                onLongItemClick = {},
                onMediaGalleryPreviewResult = {},
            )
            ChatTheme.componentFactory.GiphyAttachmentContent(
                params = GiphyAttachmentContentParams(
                    modifier = Modifier.fillMaxWidth(),
                    state = attachmentState,
                    interactive = false,
                ),
            )
        }

        Row(
            modifier = Modifier
                .padding(vertical = StreamTokens.spacingXs)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) {
            StreamTextButton(
                onClick = {
                    view.announceForAccessibility(sentAnnouncement)
                    onGiphyActionClick(SendGiphy(message))
                },
                text = stringResource(R.string.stream_compose_message_list_giphy_send),
                style = StreamButtonStyleDefaults.primaryGhost,
                modifier = Modifier
                    .weight(1f)
                    .testTag("Stream_GiphyButton_Send"),
            )

            StreamTextButton(
                onClick = {
                    view.announceForAccessibility(shuffledAnnouncement)
                    onGiphyActionClick(ShuffleGiphy(message))
                },
                text = stringResource(R.string.stream_compose_message_list_giphy_shuffle),
                style = StreamButtonStyleDefaults.secondaryGhost,
                modifier = Modifier
                    .weight(1f)
                    .testTag("Stream_GiphyButton_Shuffle"),
            )

            StreamTextButton(
                onClick = {
                    view.announceForAccessibility(cancelledAnnouncement)
                    onGiphyActionClick(CancelGiphy(message))
                },
                text = stringResource(R.string.stream_compose_message_list_giphy_cancel),
                style = StreamButtonStyleDefaults.secondaryGhost,
                modifier = Modifier
                    .weight(1f)
                    .testTag("Stream_GiphyButton_Cancel"),
            )
        }
    }
}

private const val PreviewFocusRequestDelayMs = 100L

/**
 * Observes [AccessibilityManager.isTouchExplorationEnabled] and recomposes when it toggles. Used
 * to gate focus-stealing behaviour so we only request TalkBack focus when an explore-by-touch
 * service (e.g. TalkBack) is active — otherwise we would yank Compose focus away from the
 * composer's text field for sighted users and dismiss the IME.
 */
@Composable
private fun rememberIsTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService<AccessibilityManager>() } ?: return false
    var enabled by remember(manager) { mutableStateOf(manager.isTouchExplorationEnabled) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled = it }
        manager.addTouchExplorationStateChangeListener(listener)
        enabled = manager.isTouchExplorationEnabled
        onDispose { manager.removeTouchExplorationStateChangeListener(listener) }
    }
    return enabled
}

@Composable
internal fun GiphyMessageContent() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Red.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        val attachment = Attachment(
            type = AttachmentType.GIPHY,
            titleLink = "https://giphy.com/gifs/funny-cat-3oEjI6SIIHBdRxXI40",
            title = "Hello",
        )
        Box(Modifier.background(MessageStyling.backgroundColor(true))) {
            GiphyMessageContent(
                message = Message(attachments = listOf(attachment)),
                currentUser = null,
                onGiphyActionClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun GiphyMessageContentPreview() {
    ChatTheme {
        GiphyMessageContent()
    }
}
