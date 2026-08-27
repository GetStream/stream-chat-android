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

/**
 * PoC: server-assigned message ids at attachment-upload time.
 *
 * Simulates an app whose backend assigns the message id as part of the attachment upload flow:
 * 1. [ServerIdFileUploader] "uploads" each attachment to the app's backend ([FakeCustomerBackend]) and receives
 *    the message id the message must be sent with, delivering it via
 *    [io.getstream.chat.android.models.UploadedFile.extraData].
 * 2. [ServerIdMessageTransformer] rewrites the message id right before the message is sent to the API,
 *    stashing the original local id in the message extra data.
 * 3. The message list keys items by the original local id (see the sample's CustomChatComponentFactory),
 *    so the id rewrite does not visually remove and re-add the message.
 */
object ServerIdPoc {

    /** Toggles the whole PoC on/off. */
    const val ENABLED = true

    /** Attachment extra data key carrying the backend-assigned message id from the uploader to the transformer. */
    const val KEY_REMOTE_MESSAGE_ID = "remoteMessageId"

    /** Message extra data key preserving the original local message id, used as a stable UI key. */
    const val KEY_LOCAL_ID = "serverIdPocLocalId"

    const val TAG = "ServerIdPoc"
}
