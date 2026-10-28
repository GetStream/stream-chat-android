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

package io.getstream.chat.android.client.parser2.adapters

import io.getstream.chat.android.client.api2.mapping.MENTIONED_GROUP_IDS_KEY

/**
 * Keys the generated user models declare that the domain `User` has no property for: they are kept in the overflow
 * map, so `User.extraData` still exposes them. Drop with AND-1398.
 */
internal val GENERATED_USER_EXTRA_DATA_KEYS = setOf(
    "deleted_at",
    "revoke_tokens_issued_before",
)

/**
 * Wire keys the generated model declares and the hand-written user DTO did not, so they reached
 * `User.extraData` and would otherwise stop doing so. Drop with AND-1398.
 */
internal val GENERATED_FULL_USER_EXTRA_DATA_KEYS = setOf(
    "shadow_banned",
    "ban_expires",
    "deleted_at",
    "revoke_tokens_issued_before",
)

/**
 * Keys `OwnUserResponse` declares that the domain `User` has no property for: they are kept in the overflow map,
 * so `User.extraData` still exposes them. Drop with AND-1398.
 */
internal val GENERATED_OWN_USER_EXTRA_DATA_KEYS = setOf(
    "deleted_at",
    "latest_hidden_channels",
    "revoke_tokens_issued_before",
    "total_unread_count_by_team",
)

/**
 * Keys the hand-written member DTO left undeclared, so they used to reach `Member.extraData` and would
 * otherwise stop doing so. Only `user_id` is read at all, as the fallback for the user id when the payload
 * carries no user, and that does not restore the map an app may already read. Drop with AND-1398.
 */
internal val GENERATED_MEMBER_EXTRA_DATA_KEYS = setOf(
    "deleted_at",
    "deleted_messages",
    "is_moderator",
    "role",
    "user_id",
)

/**
 * Channel fields that were only reachable through `Channel.extraData` before they became declared properties.
 *
 * TODO(AND-1398): drop in the next major, along with the [CustomObjectDtoAdapter.alsoKeepInExtraData] plumbing.
 */
internal val LEGACY_CHANNEL_EXTRA_DATA_KEYS = setOf("disabled", "blocked", "truncated_at")

/**
 * Keys `ChannelResponse` declares that the hand-written channel DTO did not, so they used to reach
 * `Channel.extraData` and would otherwise stop doing so. Kept there as well as mapped, matching how
 * [LEGACY_CHANNEL_EXTRA_DATA_KEYS] treats the keys the hand-written DTO declared. Drop with AND-1398.
 */
internal val GENERATED_CHANNEL_EXTRA_DATA_KEYS = setOf(
    "auto_translation_enabled",
    "auto_translation_language",
    "hidden",
    "hide_messages_before",
    "mute_expires_at",
    "muted",
    "truncated_by",
)

/**
 * Keys `MessageResponse` declares that the hand-written message DTO did not, so they used to reach
 * `Message.extraData` and would otherwise stop doing so. Kept there as well as mapped. Drop with AND-1398.
 */
internal val GENERATED_MESSAGE_EXTRA_DATA_KEYS = setOf(
    "draft",
    "image_labels",
    MENTIONED_GROUP_IDS_KEY,
    "mml",
    "poll_id",
    "restricted_visibility",
)
