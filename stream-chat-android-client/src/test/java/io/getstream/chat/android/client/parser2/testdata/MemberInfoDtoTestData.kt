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

import org.intellij.lang.annotations.Language

internal object MemberInfoDtoTestData {

    // notifications_muted is a plain non-omitempty tag on the payload struct, so the wire always sends it.
    @Language("JSON")
    val downstreamJson: String = """
        {
          "channel_role": "channel_member",
          "notifications_muted": false
        }
    """.trimIndent()
}
