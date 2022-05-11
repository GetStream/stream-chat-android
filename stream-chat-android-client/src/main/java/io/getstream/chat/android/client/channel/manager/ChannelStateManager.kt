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

package io.getstream.chat.android.client.channel.manager

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import java.util.Date

public interface ChannelStateManager {

    public fun upsertMessage(message: Message)

    public fun upsertMessages(messages: List<Message>)

    public fun updateAttachmentUploadState(messageId: String, uploadId: String, newState: Attachment.UploadState)

    public fun updateOldMessagesFromLocalChannel(localChannel: Channel)

    public fun updateOldMessagesFromChannel(c: Channel)

    public fun updateDataFromChannel(c: Channel)

    public fun updateChannelData(channel: Channel)

    public fun setWatcherCount(watcherCount: Int)

    public fun removeMessagesBefore(date: Date, systemMessage: Message? = null)

    public fun updateReads(reads: List<ChannelUserRead>)
}
