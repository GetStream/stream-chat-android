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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

/**
 * Defines a type of a mention inside the message composer.
 *
 * By default, only the "user" mention type is defined, which represents user mentions (e.g. "@John Doe").
 * You can extend this class to define custom mention types if needed.
 *
 * @param value The name of the mention type.
 */
public data class MentionType(public val value: String) {

    public companion object {

        /**
         * Predefined mention type for user mentions (ex. "@John Doe").
         */
        public val user: MentionType = MentionType("user")

        /**
         * Predefined mention type for `@channel` mentions, notifying every channel member.
         */
        public val channel: MentionType = MentionType("channel")

        /**
         * Predefined mention type for `@here` mentions, notifying online channel members.
         */
        public val here: MentionType = MentionType("here")

        /**
         * Predefined mention type for role mentions (e.g. `@admin`, `@moderator`).
         */
        public val role: MentionType = MentionType("role")

        /**
         * Predefined mention type for user-group mentions (e.g. `@backendsupport`).
         */
        public val group: MentionType = MentionType("group")
    }
}

/**
 * Represents a mention token inside the message composer.
 *
 * Built-in types are [User], [Channel], [Here], [Role], and [Group].
 * You can extend this interface to define custom mentions if needed.
 */
public interface Mention {

    /**
     * The type of the mention.
     *
     * @see MentionType
     */
    public val type: MentionType

    /**
     * The display text of the mention. Implementations must guarantee a non-empty value so
     * autocomplete and linkification produce a stable `@token` even when the underlying source
     * lacks a human-readable name.
     */
    public val display: String

    /**
     * A user mention.
     *
     * @param user The user being mentioned.
     */
    public data class User(public val user: io.getstream.chat.android.models.User) : Mention {
        override val type: MentionType = MentionType.user
        override val display: String = user.name.ifEmpty { user.id }
    }

    /**
     * An `@channel` mention: notifies every channel member.
     */
    public object Channel : Mention {
        override val type: MentionType = MentionType.channel
        override val display: String = "channel"
    }

    /**
     * An `@here` mention: notifies online channel members.
     */
    public object Here : Mention {
        override val type: MentionType = MentionType.here
        override val display: String = "here"
    }

    /**
     * A role mention (e.g. `@admin`).
     *
     * @param role The role name.
     */
    public data class Role(public val role: String) : Mention {
        override val type: MentionType = MentionType.role
        override val display: String = role
    }

    /**
     * A user-group mention.
     *
     * @param group The group being mentioned.
     */
    public data class Group(public val group: io.getstream.chat.android.models.UserGroup) : Mention {
        override val type: MentionType = MentionType.group
        override val display: String = group.name
    }
}
