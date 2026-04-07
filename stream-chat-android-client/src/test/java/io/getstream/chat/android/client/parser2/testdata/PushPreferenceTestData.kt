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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import org.intellij.lang.annotations.Language
import java.util.Date

internal object PushPreferenceTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"chat_level":"mentions","disabled_until":"2020-06-29T06:14:28.000Z"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{}"""

    val expectedAllFields = PushPreference(
        level = PushPreferenceLevel.mentions,
        disabledUntil = Date(1593411268000),
    )

    val expectedOptionalFieldsMissing = PushPreference(
        level = null,
        disabledUntil = null,
    )
}
