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

import io.getstream.chat.android.randomString
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper.Companion.FILE_NAME_PREFIX
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

public class StringUtilsTest {

    @Test
    public fun shouldRemoveAttachmentPrefixFromString() {
        val dateFormat = "HHmmssSSS"
        val fileName = randomString(5)

        val result = StringUtils.removeTimePrefix("${FILE_NAME_PREFIX}${dateFormat}_$fileName", dateFormat)

        result shouldBeEqualTo fileName
    }

    @Test
    public fun shouldNotChangeStringWhenPrefixIsNotPresent() {
        val dateFormat = "HHmmssSSS"
        val randomString = randomString(5)

        val result = StringUtils.removeTimePrefix(randomString, dateFormat)

        result shouldBeEqualTo randomString
    }

    @Test
    public fun shouldBeAbleNamesWithOurPrefixMark() {
        val dateFormat = "HHmmssSSS"
        val fileName = randomString(5)

        val result = StringUtils.removeTimePrefix("${FILE_NAME_PREFIX}${dateFormat}_$fileName", dateFormat)

        result shouldBeEqualTo fileName
    }
}
