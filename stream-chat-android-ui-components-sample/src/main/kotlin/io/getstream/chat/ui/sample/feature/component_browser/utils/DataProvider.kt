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

package io.getstream.chat.ui.sample.feature.component_browser.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import com.getstream.sdk.chat.model.AttachmentMetaData
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi

internal fun drawableResToUri(context: Context, @DrawableRes drawableResId: Int): String {
    val res = context.resources
    return ContentResolver.SCHEME_ANDROID_RESOURCE +
        "://" + res.getResourcePackageName(drawableResId) +
        '/' + res.getResourceTypeName(drawableResId) +
        '/' + res.getResourceEntryName(drawableResId)
}

internal fun randomUser(withImage: Boolean = true, isOnline: Boolean = true): User {
    return User().apply {
        id = "${('A'..'Z').random()}${('A'..'Z').random()}"
        name = "${('A'..'Z').random()} ${('A'..'Z').random()}"

        if (withImage) {
            image = randomImageUrl()
        }
        if (isOnline) {
            online = true
        }
    }
}

internal fun randomUsers(size: Int = 30): List<User> {
    return 0.until(size).map { randomUser() }
}

internal fun randomChannel(members: List<Member> = emptyList()): Channel {
    return Channel().apply {
        cid = "${('A'..'Z').random()} ${('A'..'Z').random()}"
        name = "Sample Channel"
        this.members = members
    }
}

internal fun randomMember(withImage: Boolean = true): Member {
    return Member(user = randomUser(withImage))
}

internal fun randomMessage(): Message {
    return Message().apply {
        text = "Random message"
    }
}

@ExperimentalStreamChatApi
internal fun randomMediaAttachments(count: Int): List<AttachmentMetaData> {
    return List(count) {
        AttachmentMetaData(
            uri = Uri.parse(randomImageUrl()),
            type = "image",
            mimeType = "image/png"
        )
    }
}

@ExperimentalStreamChatApi
internal fun randomFileAttachments(count: Int): List<AttachmentMetaData> {
    return List(count) {
        AttachmentMetaData(
            uri = Uri.parse(randomImageUrl()),
            type = "file",
            mimeType = "application/pdf",
        ).apply {
            size = 100000L
            title = "Sample PDF"
        }
    }
}

internal fun randomCommand(): Command {
    return Command("giphy", "Post a random gif to the channel", "[text]", "fun_set")
}

internal fun randomImageUrl(): String {
    val category = listOf("men", "women").random()
    val index = (0..99).random()
    return "https://randomuser.me/api/portraits/$category/$index.jpg"
}
