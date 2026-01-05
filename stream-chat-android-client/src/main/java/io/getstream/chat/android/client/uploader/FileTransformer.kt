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

package io.getstream.chat.android.client.uploader

import androidx.annotation.WorkerThread
import java.io.File

/**
 * FileTransformer is responsible for transforming the file before uploading it.
 * This can be used to compress images or videos before uploading them.
 * The transformed file will be uploaded to the CDN.
 * The original file will not be modified.
 */
public interface FileTransformer {

    /**
     * Transforms the [file] before uploading it.
     * This can be used to compress images or videos before uploading them.
     * It is safe to block
     *
     * @return The transformed file.
     */
    @WorkerThread
    public fun transform(file: File): File
}
