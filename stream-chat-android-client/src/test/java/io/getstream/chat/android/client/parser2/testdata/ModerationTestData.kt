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

import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction
import org.intellij.lang.annotations.Language

internal object ModerationTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"action":"bounce","original_text":"This is offensive","text_harms":["profanity","harassment"],"image_harms":["nudity"],"blocklist_matched":"custom_blocklist","semantic_filter_matched":"hate_speech","platform_circumvented":true}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{"action":"flag","original_text":"Some text"}"""

    @Language("JSON")
    val jsonMissingAction =
        """{"original_text":"Some text","text_harms":["profanity"],"image_harms":["nudity"],"blocklist_matched":"custom_blocklist","semantic_filter_matched":"hate_speech","platform_circumvented":true}"""

    @Language("JSON")
    val jsonMissingOriginalText =
        """{"action":"bounce","text_harms":["profanity"],"image_harms":["nudity"],"blocklist_matched":"custom_blocklist","semantic_filter_matched":"hate_speech","platform_circumvented":true}"""

    val expectedAllFields = Moderation(
        action = ModerationAction.bounce,
        originalText = "This is offensive",
        textHarms = listOf("profanity", "harassment"),
        imageHarms = listOf("nudity"),
        blocklistMatched = "custom_blocklist",
        semanticFilterMatched = "hate_speech",
        platformCircumvented = true,
    )

    val expectedOptionalFieldsMissing = Moderation(
        action = ModerationAction.flag,
        originalText = "Some text",
        textHarms = emptyList(),
        imageHarms = emptyList(),
        blocklistMatched = null,
        semanticFilterMatched = null,
        platformCircumvented = false,
    )
}
