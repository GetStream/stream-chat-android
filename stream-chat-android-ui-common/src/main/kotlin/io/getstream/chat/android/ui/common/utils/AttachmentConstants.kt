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

package io.getstream.chat.android.ui.common.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Various constants regarding default attachment limits.
 */
@InternalStreamChatApi
public object AttachmentConstants {

    /**
     * Default max upload size in MB.
     */
    public const val MAX_UPLOAD_SIZE_IN_MB: Int = 100

    /**
     * Default max number of attachments.
     */
    public const val MAX_ATTACHMENTS_COUNT: Int = 30

    /**
     * Default max size in bytes before showing a download warning.
     */
    public const val MAX_SIZE_BEFORE_DOWNLOAD_WARNING_IN_BYTES: Long = 10 * 1024 * 1024 // 10 MB
}
