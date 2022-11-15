/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

@file:JvmName("ContentUtils")

package io.getstream.chat.android.client.models

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.CustomObject
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

public val Channel.initials: String
    get() = name.initials()

public val User.initials: String
    get() = name.initials()

public fun Message.getTranslation(language: String): String {
    return i18n.get("${language}_text", "")
}

public val Message.originalLanguage: String
    get() = i18n.get("language", "")

public fun Channel.getUnreadMessagesCount(forUserId: String = ""): Int {
    return if (forUserId.isEmpty()) {
        read.sumOf { it.unreadMessages }
    } else {
        read
            .filter { it.user.id == forUserId }
            .sumOf { it.unreadMessages }
    }
}

internal fun getExternalField(obj: CustomObject, key: String): String {

    val value = obj.extraData[key]
    val emptyResult = ""

    return if (value == null) {
        emptyResult
    } else {
        if (value is String) {
            value
        } else {
            emptyResult
        }
    }
}

internal fun <A, B> Map<A, B>.get(key: A, default: B): B {
    return get(key) ?: default
}

internal fun String.initials(): String {
    return trim()
        .split("\\s+".toRegex())
        .take(2)
        .joinToString(separator = "") { it.take(1).uppercase() }
}
