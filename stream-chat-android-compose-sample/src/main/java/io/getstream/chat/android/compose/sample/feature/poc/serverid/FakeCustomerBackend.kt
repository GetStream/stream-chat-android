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

package io.getstream.chat.android.compose.sample.feature.poc.serverid

import java.util.UUID

/**
 * In-memory stand-in for an app backend whose upload endpoint also assigns the id of the message
 * that will contain the uploaded file.
 */
object FakeCustomerBackend {

    /**
     * Upload-related information returned by the backend for a single file.
     *
     * @property remoteMessageId The id the message containing the file must be sent with.
     */
    data class UploadInfo(
        val remoteMessageId: String,
    )

    /** Simulates the per-file "request upload info" endpoint. */
    fun requestUploadInfo(): UploadInfo = UploadInfo(remoteMessageId = "srv-${UUID.randomUUID()}")
}
