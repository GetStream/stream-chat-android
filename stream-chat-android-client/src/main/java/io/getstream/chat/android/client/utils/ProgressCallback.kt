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

package io.getstream.chat.android.client.utils

import io.getstream.result.Error

/**
 * Callback to listen for file upload status.
 */
public interface ProgressCallback {

    /**
     * Called when a file is uploaded successfully with an [url].
     */
    public fun onSuccess(url: String?) {
        // no-op
    }

    /**
     * Called when a file could not be uploaded due to cancellation, network problem or timeout etc
     * with an [error].
     *
     * @see Error
     */
    public fun onError(error: Error) {
        // no-op
    }

    /**
     * Called when the file upload is in progress with [bytesUploaded] count
     * and [totalBytes] in bytes of the file.
     */
    public fun onProgress(bytesUploaded: Long, totalBytes: Long) {
        // no-op
    }
}
