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

import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageModerationDetails
import org.intellij.lang.annotations.Language

internal object MessageModerationDetailsTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"original_text":"This is offensive","action":"MESSAGE_RESPONSE_ACTION_BOUNCE","error_msg":"Message contains prohibited content"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{}"""

    val expectedAllFields = MessageModerationDetails(
        originalText = "This is offensive",
        action = MessageModerationAction.bounce,
        errorMsg = "Message contains prohibited content",
    )

    val expectedOptionalFieldsMissing = MessageModerationDetails(
        originalText = "",
        action = MessageModerationAction(""),
        errorMsg = "",
    )
}
