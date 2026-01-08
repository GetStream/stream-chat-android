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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState

/**
 * Parameters for the [ChatComponentFactory.MessageReactionList] component.
 *
 * @param modifier Modifier for styling.
 * @param message The message for which the reactions are displayed.
 * @param reactions The list of reaction options to display.
 * @param onClick Handler when the reaction list is clicked. The message is provided as a parameter.
 */
public data class MessageReactionListParams(
    val modifier: Modifier = Modifier,
    val message: Message,
    val reactions: List<ReactionOptionItemState>,
    val onClick: ((message: Message) -> Unit)? = null,
)

/**
 * Parameters for the [ChatComponentFactory.MessageReactionItem] component.
 *
 * @param modifier Modifier for styling.
 * @param state The reaction option state, holding all information required to render the icon.
 */
public data class MessageReactionItemParams(
    val modifier: Modifier = Modifier,
    val state: ReactionOptionItemState,
)

/**
 * Parameters for the [ChatComponentFactory.ChannelMediaAttachmentsPreviewBottomBar] component.
 *
 * @param centerContent Composable lambda for center content in the bottom bar.
 * @param leadingContent Composable lambda for leading content in the bottom bar.
 * @param trailingContent Composable lambda for trailing content in the bottom bar.
 */
public data class ChannelMediaAttachmentsPreviewBottomBarParams(
    val centerContent: @Composable () -> Unit,
    val leadingContent: @Composable () -> Unit = {},
    val trailingContent: @Composable () -> Unit = {},
)

/**
 * Parameters for the [ChatComponentFactory.MessageFooterStatusIndicator] component.
 *
 * @param messageItem The message item state.
 * @param modifier Modifier for styling.
 */
public data class MessageFooterStatusIndicatorParams(
    val messageItem: MessageItemState,
    val modifier: Modifier = Modifier,
)
