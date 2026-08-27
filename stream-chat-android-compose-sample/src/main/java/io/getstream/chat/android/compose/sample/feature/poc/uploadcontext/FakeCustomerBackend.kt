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

package io.getstream.chat.android.compose.sample.feature.poc.uploadcontext

import android.util.Log

/**
 * In-memory stand-in for an app backend that stores uploaded files and links them to the message that will
 * contain them, keyed by the Stream message id it receives at upload time.
 */
object FakeCustomerBackend {

    private val uploadsByMessageId = mutableMapOf<String, MutableList<String>>()

    /** Simulates uploading [fileName] and linking it to the message with [messageId]. */
    fun registerUpload(messageId: String?, fileName: String) {
        if (messageId == null) {
            Log.w(UploadContextPoc.TAG, "Upload of $fileName is not linked to any message")
            return
        }
        val uploads = uploadsByMessageId.getOrPut(messageId) { mutableListOf() }
        uploads += fileName
        Log.d(UploadContextPoc.TAG, "Linked $fileName to message $messageId (uploads: $uploads)")
    }
}
