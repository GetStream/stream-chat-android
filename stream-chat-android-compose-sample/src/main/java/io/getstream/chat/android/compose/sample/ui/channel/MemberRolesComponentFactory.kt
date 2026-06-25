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

package io.getstream.chat.android.compose.sample.ui.channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.RoleBadge
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.GroupChannelInfoMemberTrailingContentParams
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.common.R as UiCommonR

/**
 * Sample [ChatComponentFactory] that shows multiple role badges per member in the group channel
 * info screen. The SDK shows a single role badge, this override demonstrates the multi-role layout.
 *
 * The mute icon stays provided by the SDK (in the center content), this override only replaces the
 * trailing role badges.
 */
internal object MemberRolesComponentFactory : ChatComponentFactory {

    @Composable
    override fun GroupChannelInfoMemberTrailingContent(params: GroupChannelInfoMemberTrailingContentParams) {
        MemberRolesTrailingContent(params)
    }
}

/**
 * Renders all of a member's role badges (owner, admin and channel role) side by side, reusing the
 * SDK [RoleBadge] and label color tokens.
 */
@Composable
internal fun MemberRolesTrailingContent(params: GroupChannelInfoMemberTrailingContentParams) {
    val roleLabels = memberRoleLabels(member = params.member, isOwner = params.isOwner)
    if (roleLabels.isNotEmpty()) {
        Row(
            modifier = params.modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            roleLabels.forEach { label ->
                RoleBadge(
                    text = label.text,
                    backgroundColor = if (label.isPrimary) {
                        ChatTheme.colors.brand.s150
                    } else {
                        ChatTheme.colors.chrome.s150
                    },
                    textColor = if (label.isPrimary) {
                        ChatTheme.colors.brand.s900
                    } else {
                        ChatTheme.colors.textPrimary
                    },
                )
            }
        }
    }
}

/**
 * Builds the list of role labels for a member. Badges come from three independent sources:
 * ownership (derived from the channel creator), the global user role (admin), and the channel role
 * (moderator or a custom role). The default `channel_member` role is not labeled.
 */
@Composable
private fun memberRoleLabels(member: Member, isOwner: Boolean): List<RoleLabel> {
    val ownerLabel = stringResource(id = UiCommonR.string.stream_ui_channel_info_member_owner)
    val adminLabel = stringResource(id = UiCommonR.string.stream_ui_channel_info_member_admin)
    val moderatorLabel = stringResource(id = UiCommonR.string.stream_ui_channel_info_member_moderator)
    return buildList {
        if (isOwner) {
            add(RoleLabel(text = ownerLabel, isPrimary = true))
        }
        if (member.user.role == "admin") {
            add(RoleLabel(text = adminLabel, isPrimary = false))
        }
        when (val role = member.channelRole) {
            "channel_moderator" -> add(RoleLabel(text = moderatorLabel, isPrimary = false))
            null, "", "owner", "admin", "channel_member" -> Unit
            else -> add(RoleLabel(text = role, isPrimary = false))
        }
    }
}

/**
 * A member role to render as a [RoleBadge].
 *
 * @property text The localized role label.
 * @property isPrimary Whether the badge uses the primary (owner) color, otherwise neutral.
 */
private data class RoleLabel(
    val text: String,
    val isPrimary: Boolean,
)
