/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
    }
}

/**
 * Represents a mention token inside the message composer.
 *
 * By default, only user mentions are supported.
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
     * The display text of the mention.
     */
    public val display: String

    /**
     * Represents a user mention inside the message composer.
     *
     * @param user The user being mentioned.
     */
    public data class User(public val user: io.getstream.chat.android.models.User) : Mention {
        override val type: MentionType = MentionType.user
        override val display: String = user.name
    }
}
