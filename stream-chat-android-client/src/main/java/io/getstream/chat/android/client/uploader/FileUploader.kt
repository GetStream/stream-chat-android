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
import io.getstream.chat.android.models.UploadedFile
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import io.getstream.result.Result
import java.io.File

/**
 * The FileUploader is responsible for sending and deleting files from given channel
 */
public interface FileUploader {

    /**
     * Uploads a file for the given channel. Progress can be accessed via [callback].
     *
     * @return The [Result] object containing an instance of [UploadedFile] in the case of a successful upload
     * or an exception if the upload had failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    @Suppress("LongParameterList")
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile>

    /**
     * Uploads a file for the given channel.
     *
     * @return The [Result] object containing an instance of [UploadedFile] in the case of a successful upload
     * or an exception if the upload had failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    public fun sendFile(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile>

    /**
     * Uploads an image for the given channel. Progress can be accessed via [callback].
     *
     * @return The [Result] object containing an instance of [UploadedFile] in the case of a successful upload
     * or an exception if the upload had failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    @Suppress("LongParameterList")
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
        callback: ProgressCallback,
    ): Result<UploadedFile>

    /**
     * Uploads an image for the given channel.
     *
     * @return The [Result] object containing an instance of [UploadedFile] in the case of a successful upload
     * or an exception if the upload had failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    public fun sendImage(
        channelType: String,
        channelId: String,
        userId: String,
        file: File,
    ): Result<UploadedFile>

    /**
     * Deletes the file represented by [url] from the given channel.
     *
     * @return The empty [Result] object, or [Result] object with exception if the operation failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    public fun deleteFile(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit>

    /**
     * Deletes the image represented by [url] from the given channel.
     *
     * @return The empty [Result] object, or [Result] object with exception if the operation failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    public fun deleteImage(
        channelType: String,
        channelId: String,
        userId: String,
        url: String,
    ): Result<Unit>

    /**
     * Uploads a file not related to any channel. Progress can be accessed via [progressCallback].
     *
     * @param file The file to be uploaded.
     * @param user An optional user associated with the file. Can be null.
     * @param progressCallback The callback to be invoked periodically to report upload progress.
     * @return The [Result] object containing an instance of [UploadedFile] in the case of a successful upload
     * or an exception if the upload failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    public fun uploadFile(
        file: File,
        user: User?,
        progressCallback: ProgressCallback?,
    ): Result<UploadedFile> = error("Not implemented! Have you forgotten to implement it in your custom FileUploader?")

    /**
     * Deletes a file not related to any channel.
     *
     * @param url The URL of the file to be deleted.
     * @param userId An optional ID of the user associated with the file.
     * @return The empty [Result] object, or [Result] object with exception if the operation failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    public fun deleteFile(
        url: String,
        userId: UserId?,
    ): Result<Unit> = error("Not implemented! Have you forgotten to implement it in your custom FileUploader?")

    /**
     * Uploads an image not related to any channel. Progress can be accessed via [progressCallback].
     *
     * @param file The image to be uploaded.
     * @param user An optional user associated with the image. Can be null.
     * @param progressCallback The callback to be invoked periodically to report upload progress.
     * @return The [Result] object containing an instance of [UploadedFile] in the case of a successful upload
     * or an exception if the upload failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    public fun uploadImage(
        file: File,
        user: User?,
        progressCallback: ProgressCallback?,
    ): Result<UploadedFile> = error("Not implemented! Have you forgotten to implement it in your custom FileUploader?")

    /**
     * Deletes an image not related to any channel.
     *
     * @param url The URL of the image to be deleted.
     * @param userId An optional ID of the user associated with the image.
     * @return The empty [Result] object, or [Result] object with exception if the operation failed.
     *
     * @see [Result.success]
     * @see [Result.failure]
     */
    public fun deleteImage(
        url: String,
        userId: UserId?,
    ): Result<Unit> = error("Not implemented! Have you forgotten to implement it in your custom FileUploader?")
}
