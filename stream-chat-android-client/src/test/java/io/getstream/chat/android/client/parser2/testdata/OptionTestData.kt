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

import io.getstream.chat.android.models.Option
import org.intellij.lang.annotations.Language

internal object OptionTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"id":"option1","text":"First option","custom_field":"custom_value"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{"id":"option1","text":"First option"}"""

    @Language("JSON")
    val jsonMissingId =
        """{"text":"First option"}"""

    @Language("JSON")
    val jsonMissingText =
        """{"id":"option1"}"""

    val expectedAllFields = Option(
        id = "option1",
        text = "First option",
        extraData = mapOf("custom_field" to "custom_value"),
    )

    val expectedOptionalFieldsMissing = Option(
        id = "option1",
        text = "First option",
        extraData = emptyMap(),
    )
}
