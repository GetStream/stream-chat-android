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

package io.getstream.chat.android.compose.ui.components.poll

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatarStack
import io.getstream.chat.android.compose.ui.components.common.RadioCheck
import io.getstream.chat.android.compose.ui.messages.list.LocalMessageOnLongClick
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling.PollStyle
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.VotingVisibility

/**
 * Renders a single poll option as a voting row: a leading radio / checkbox, the option text, a
 * preview of voter avatars, the vote count, and a progress bar.
 *
 * The whole row is a single TalkBack focus exposing the [Role.RadioButton] or [Role.Checkbox]
 * role (depending on [Poll.maxVotesAllowed]) with the option text and vote count. Toggling fires
 * [onCastVote] or [onRemoveVote] following the same rules as the inline poll on the message
 * screen.
 *
 * Shared by the inline poll on the message screen and the more-options bottom sheet — both
 * delegate here so the a11y wiring is implemented once.
 *
 * @param poll The poll the option belongs to (drives role + cast/remove gating via
 * [Poll.maxVotesAllowed] and [Poll.closed]).
 * @param option The option rendered by this row.
 * @param voteCount Number of votes the option has received.
 * @param totalVoteCount Total votes across all options, used to compute the progress fill.
 * @param users Subset of users whose avatars are previewed when voting visibility is public.
 * @param checkedCount Number of options the current user has already voted for, used together
 * with [Poll.maxVotesAllowed] to decide whether another vote can be cast.
 * @param checked Whether the current user has voted for this option.
 * @param style Colours used for text, radio outline, progress fill and track.
 * @param onCastVote Invoked when the user casts a vote for this option.
 * @param onRemoveVote Invoked when the user removes their vote from this option.
 * @param modifier Modifier applied to the row container.
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongParameterList", "LongMethod")
@Composable
internal fun PollOptionVotingRow(
    poll: Poll,
    option: Option,
    voteCount: Int,
    totalVoteCount: Int,
    users: List<User>,
    checkedCount: Int,
    checked: Boolean,
    style: PollStyle,
    onCastVote: () -> Unit,
    onRemoveVote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val toggleRole = if (poll.maxVotesAllowed == 1) Role.RadioButton else Role.Checkbox
    val onToggle: (Boolean) -> Unit = { enabled ->
        val canVote = poll.maxVotesAllowed?.let { checkedCount < it } ?: true
        if (enabled && canVote && !checked) {
            onCastVote()
        } else if (!enabled) {
            onRemoveVote()
        }
    }
    // Forward long-press up to the message row's actions-menu handler. Without this, the
    // toggle would consume the long-press as a tap and TalkBack's double-tap-and-hold would
    // never open the actions menu over a poll option.
    val onMessageLongClick = LocalMessageOnLongClick.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .applyIf(!poll.closed) {
                combinedClickable(
                    role = toggleRole,
                    onClick = { onToggle(!checked) },
                    onLongClick = onMessageLongClick,
                )
                    // `combinedClickable` sets the role but not the toggle state — restore the
                    // "checked" / "not checked" announce that the previous `toggleable` modifier
                    // contributed so TalkBack still reads the current vote state on each option.
                    .semantics {
                        toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                    }
            },
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!poll.closed) {
            RadioCheck(
                modifier = Modifier.semantics { hideFromAccessibility() },
                checked = checked,
                onCheckedChange = onToggle,
                borderColor = style.outlineColor,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs)) {
            Row(Modifier.heightIn(min = AvatarSize.ExtraSmall)) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = option.text,
                    style = ChatTheme.typography.captionDefault,
                    color = style.textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (users.isNotEmpty() && poll.votingVisibility != VotingVisibility.ANONYMOUS) {
                    UserAvatarStack(
                        overlap = StreamTokens.spacingXs,
                        users = users.take(MaxStackedAvatars),
                        avatarSize = AvatarSize.ExtraSmall,
                        modifier = Modifier.padding(start = StreamTokens.spacingXs, end = StreamTokens.spacing2xs),
                    )
                }

                val voteCountDescription = pluralStringResource(
                    R.plurals.stream_compose_poll_vote_counts,
                    voteCount,
                    voteCount,
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .semantics { contentDescription = voteCountDescription },
                    text = voteCount.toString(),
                    style = ChatTheme.typography.metadataDefault,
                    color = style.textColor,
                )
            }

            val progress by animateFloatAsState(
                targetValue = if (voteCount == 0 || totalVoteCount == 0) {
                    0f
                } else {
                    voteCount / totalVoteCount.toFloat()
                },
            )

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clearAndSetSemantics {},
                progress = { progress },
                color = style.progressColor,
                trackColor = style.trackColor,
                gapSize = 0.dp,
                strokeCap = StrokeCap.Square,
                drawStopIndicator = { /* Don't draw the stop indicator */ },
            )
        }
    }
}

private const val MaxStackedAvatars = 3
