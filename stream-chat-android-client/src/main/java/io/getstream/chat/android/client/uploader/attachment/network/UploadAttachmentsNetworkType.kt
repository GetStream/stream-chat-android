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
 
package io.getstream.chat.android.client.uploader.attachment.network

import androidx.work.NetworkType

/**
 * An enumeration of various network types used as a constraint in [io.getstream.chat.android.offline.internal.message.attachments.UploadAttachmentsAndroidWorker].
 */
public enum class UploadAttachmentsNetworkType {
    /**
     * Any working network connection is required.
     */
    CONNECTED,

    /**
     * An unmetered network connection is required.
     */
    UNMETERED,

    /**
     * A non-roaming network connection is required.
     */
    NOT_ROAMING,

    /**
     * A metered network connection is required.
     */
    METERED;

    internal fun toNetworkType(): NetworkType = when (this) {
        CONNECTED -> NetworkType.CONNECTED
        UNMETERED -> NetworkType.UNMETERED
        NOT_ROAMING -> NetworkType.NOT_ROAMING
        METERED -> NetworkType.METERED
    }
}
