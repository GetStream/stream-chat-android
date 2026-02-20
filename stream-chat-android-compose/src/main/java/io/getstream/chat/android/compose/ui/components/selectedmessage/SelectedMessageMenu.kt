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

package io.getstream.chat.android.compose.ui.components.selectedmessage

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.state.messages.MessageAlignment
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.messages.list.LocalSelectedMessageBounds
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ReactionSortingByLastReactionAt
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Represents the options user can take after selecting a message.
 *
 * The selected message is shown in a centered pop-out overlay with a dark background,
 * reactions above it and a flat options list below.
 *
 * @param message The selected message.
 * @param messageOptions The available message options within the menu.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 * @param onMessageAction Handler that propagates click events on each item.
 * @param onShowMoreReactionsSelected Handler that propagates clicks on the show more reactions button.
 * @param modifier Modifier for styling.
 * @param currentUser The currently logged-in user, used to build the message preview.
 * @param onDismiss Handler called when the menu is dismissed.
 */
@Suppress("LongMethod")
@Composable
public fun SelectedMessageMenu(
    message: Message,
    messageOptions: List<MessageOptionItemState>,
    ownCapabilities: Set<String>,
    onMessageAction: (MessageAction) -> Unit,
    onShowMoreReactionsSelected: () -> Unit,
    modifier: Modifier = Modifier,
    currentUser: User? = null,
    onDismiss: () -> Unit = {},
) {
    val messageItemState = MessageItemState(
        message = message,
        isMine = message.user.id == currentUser?.id,
        currentUser = currentUser,
        ownCapabilities = ownCapabilities,
        showMessageFooter = false,
    )
    val messageAlignment = ChatTheme.messageAlignmentProvider.provideMessageAlignment(messageItemState)
    val bubbleAlignmentPadding = when (messageAlignment) {
        MessageAlignment.Start -> Modifier.padding(start = 40.dp)
        MessageAlignment.End -> Modifier.padding(end = 8.dp)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        (LocalView.current.parent as? DialogWindowProvider)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply {
                    blurBehindRadius = BackgroundBlur
                }
            }
            window.setDimAmount(DimAmount)
        }

        val isInspection = LocalInspectionMode.current
        val animation = rememberMenuAnimation(
            sourceBounds = LocalSelectedMessageBounds.current?.value,
            messageAlignment = messageAlignment,
        )

        LaunchedEffect(Unit) {
            if (isInspection) animation.snapIn() else animation.animateIn()
        }

        Column(
            modifier = modifier
                .semantics { testTagsAsResourceId = true }
                .fillMaxSize()
                .clickable(onClick = onDismiss, indication = null, interactionSource = null)
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(StreamTokens.spacingXs),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = messageAlignment.contentAlignment,
        ) {
            val canLeaveReaction = ChannelCapabilities.SEND_REACTION in ownCapabilities
            if (canLeaveReaction && ChatTheme.reactionOptionsTheme.areReactionOptionsVisible) {
                ChatTheme.componentFactory.MessageMenuHeaderContent(
                    modifier = bubbleAlignmentPadding.then(animation.peripheralModifier(slideY = (-24).dp)),
                    message = message,
                    messageOptions = messageOptions,
                    onMessageAction = onMessageAction,
                    ownCapabilities = ownCapabilities,
                    onShowMore = onShowMoreReactionsSelected,
                )
            }
            Box(
                modifier = Modifier
                    .padding(vertical = StreamTokens.spacingXs)
                    .then(animation.messageModifier),
            ) {
                ChatTheme.componentFactory.MessageContainer(
                    modifier = Modifier.clipToBounds(),
                    messageItem = messageItemState,
                    reactionSorting = ReactionSortingByLastReactionAt,
                    onPollUpdated = { _, _ -> },
                    onCastVote = { _, _, _ -> },
                    onRemoveVote = { _, _, _ -> },
                    selectPoll = { _, _, _ -> },
                    onClosePoll = {},
                    onAddPollOption = { _, _ -> },
                    onLongItemClick = {},
                    onThreadClick = {},
                    onReactionsClick = {},
                    onGiphyActionClick = {},
                    onMediaGalleryPreviewResult = {},
                    onQuotedMessageClick = {},
                    onUserAvatarClick = null,
                    onMessageLinkClick = null,
                    onUserMentionClick = {},
                    onAddAnswer = { _, _, _ -> },
                    onReply = {},
                )
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = onDismiss, indication = null, interactionSource = null),
                )
            }

            ChatTheme.componentFactory.MessageMenuOptions(
                modifier = bubbleAlignmentPadding.then(animation.peripheralModifier(slideY = 24.dp)),
                message = message,
                options = messageOptions,
                onMessageOptionSelected = { onMessageAction(it.action) },
            )
        }
    }
}

private const val BackgroundBlur = 50
private const val DimAmount = 0.6f

/**
 * Holds the animation state for the [SelectedMessageMenu] pop-out effect.
 *
 * Two parallel animations drive the menu entrance:
 * - **message**: slides the message from its list position to the dialog center
 * - **peripheral**: fades + slides in reactions header and options list
 *
 * @param sourceBounds Window-space bounds of the message in the list.
 * @param messageAlignment The alignment of the message in the list.
 */
@Stable
private class MenuAnimationState(
    private val sourceBounds: Rect?,
    private val messageAlignment: MessageAlignment,
) {
    private val message = Animatable(0f)
    private val peripheral = Animatable(0f)

    private var targetBounds: Rect? by mutableStateOf(null)

    val messageModifier: Modifier
        get() = Modifier
            .onGloballyPositioned { coords ->
                if (coords.isAttached && targetBounds == null) {
                    targetBounds = coords.boundsInWindow()
                }
            }
            .graphicsLayer {
                val source = sourceBounds
                val target = targetBounds
                if (source != null && target != null) {
                    val progress = message.value
                    translationX = when (messageAlignment) {
                        MessageAlignment.Start -> lerp(source.left - target.left, 0f, progress)
                        MessageAlignment.End -> lerp(source.right - target.right, 0f, progress)
                    }
                    translationY = lerp(source.top - target.top, 0f, progress)
                }
            }

    fun peripheralModifier(slideY: Dp): Modifier =
        Modifier.graphicsLayer {
            alpha = peripheral.value
            translationY = (1f - peripheral.value) * slideY.toPx()
        }

    suspend fun animateIn() {
        coroutineScope {
            launch { message.animateTo(1f, tween(durationMillis = 300, easing = EaseOutCubic)) }
            launch { peripheral.animateTo(1f, tween(durationMillis = 200, delayMillis = 150)) }
        }
    }

    suspend fun snapIn() {
        coroutineScope {
            launch { message.snapTo(1f) }
            launch { peripheral.snapTo(1f) }
        }
    }
}

@Composable
private fun rememberMenuAnimation(sourceBounds: Rect?, messageAlignment: MessageAlignment): MenuAnimationState =
    remember { MenuAnimationState(sourceBounds, messageAlignment) }

@Preview(showBackground = true)
@Composable
private fun SelectedMessageMenuForIncomingMessagePreview() {
    ChatTheme {
        SelectedMessageMenuForIncomingMessage()
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedMessageMenuForOutgoingMessagePreview() {
    ChatTheme {
        SelectedMessageMenuForOutgoingMessage()
    }
}

@Composable
internal fun SelectedMessageMenuForIncomingMessage() {
    SelectedMessageMenuPreview(
        selectedMessage = PreviewMessageData.message1,
    )
}

@Composable
internal fun SelectedMessageMenuForOutgoingMessage() {
    SelectedMessageMenuPreview(
        selectedMessage = PreviewMessageData.message1.copy(user = PreviewUserData.user1),
    )
}

@Composable
private fun SelectedMessageMenuPreview(selectedMessage: Message) {
    val messageOptions = defaultMessageOptionsState(
        selectedMessage = selectedMessage,
        currentUser = PreviewUserData.user1,
        isInThread = false,
        ownCapabilities = ChannelCapabilities.toSet(),
    )

    SelectedMessageMenu(
        message = selectedMessage,
        messageOptions = messageOptions,
        onMessageAction = {},
        onShowMoreReactionsSelected = {},
        ownCapabilities = ChannelCapabilities.toSet(),
        currentUser = PreviewUserData.user1,
    )
}
