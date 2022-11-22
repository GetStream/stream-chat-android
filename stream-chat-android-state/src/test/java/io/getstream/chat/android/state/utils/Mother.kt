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

package io.getstream.chat.android.state.utils

import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomFile
import java.util.UUID

internal fun randomAttachmentsWithFile(
    size: Int = positiveRandomInt(10),
    createAttachment: (Int) -> Attachment = {
        Attachment(upload = randomFile()).apply {
            uploadId = "upload_id_${UUID.randomUUID()}"
        }
    },
): List<Attachment> = (1..size).map(createAttachment)
