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

package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import java.io.File

/**
 * The FileUploader is responsible for sending and deleting files from given channel
 */
public interface FileUploader {

    /**
     * Uploads a file for the given channel. Progress can be accessed via [callback].
     *
     * @return The [Result] object with the URL of the uploaded file, or [Result] object with exception if the
     * upload failed.
     *
     * @see [Result.success]
     * @see [Result.error]
     */
    @Suppress("LongParameterList")
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<String>

    /**
     * Uploads a file for the given channel.
     *
     * @return The [Result] object with the URL of the uploaded file, or [Result] object with exception if the
     * upload failed.
     *
     * @see [Result.success]
     * @see [Result.error]
     */
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String>

    /**
     * Uploads an image for the given channel. Progress can be accessed via [callback].
     *
     * @return The [Result] object with the URL of the uploaded image, or [Result] object with exception if the
     * upload failed.
     *
     * @see [Result.success]
     * @see [Result.error]
     */
    @Suppress("LongParameterList")
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<String>

    /**
     * Uploads an image for the given channel.
     *
     * @return The [Result] object with the URL of the uploaded image, or [Result] object with exception if the
     * upload failed.
     *
     * @see [Result.success]
     * @see [Result.error]
     */
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        file: File,
    ): Result<String>

    /**
     * Deletes the file represented by [url] from the given channel.
     *
     * @return The empty [Result] object, or [Result] object with exception if the operation failed.
     *
     * @see [Result.success]
     * @see [Result.error]
     */
    public fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ): Result<Unit>

    /**
     * Deletes the image represented by [url] from the given channel.
     *
     * @return The empty [Result] object, or [Result] object with exception if the operation failed.
     *
     * @see [Result.success]
     * @see [Result.error]
     */
    public fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        connectionId: String,
        url: String,
    ): Result<Unit>
}
