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

import io.getstream.chat.android.ui.common.helper.internal.StorageHelper.Companion.FILE_NAME_PREFIX

internal object StringUtils {

    fun removeTimePrefix(attachmentName: String?, usedDateFormat: String): String? {
        val dataFormatSize = usedDateFormat.length + 1
        val regex = "${FILE_NAME_PREFIX}\\S{$dataFormatSize}".toRegex()

        return if (attachmentName?.contains(regex) == true) {
            attachmentName.replaceFirst(regex, "")
        } else {
            attachmentName
        }
    }
}
