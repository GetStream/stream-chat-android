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

package io.getstream.chat.android.client

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.models.User
import org.mockito.kotlin.mock
import java.util.UUID

internal object Mother {
    private val fixture: JFixture
        get() = JFixture()

    fun randomAttachment(attachmentBuilder: Attachment.() -> Unit = { }): Attachment {
        return KFixture(fixture) {
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Attachment>().apply(attachmentBuilder)
    }

    fun randomChannel(channelBuilder: Channel.() -> Unit = { }): Channel {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
            sameInstance(Message::class.java, mock())
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Channel>().apply(channelBuilder)
    }

    fun randomUser(userBuilder: User.() -> Unit = { }): User {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
        } <User>().apply(userBuilder)
    }

    fun randomString(): String = UUID.randomUUID().toString()

    fun randomDevice(
        token: String = randomString(),
        pushProvider: PushProvider = PushProvider.values().random(),
    ): Device =
        Device(
            token = token,
            pushProvider = pushProvider,
        )

    fun randomUserPresenceChangedEvent(user: User = randomUser()): UserPresenceChangedEvent {
        return KFixture(fixture) {
            sameInstance(User::class.java, user)
        }()
    }
}
