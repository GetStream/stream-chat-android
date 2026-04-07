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

import io.getstream.chat.android.models.ChannelInfo
import org.intellij.lang.annotations.Language

internal object ChannelInfoTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"cid":"messaging:123","id":"123","member_count":5,"name":"General","type":"messaging","image":"https://example.com/img.png"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{}"""

    val expectedAllFields = ChannelInfo(
        cid = "messaging:123",
        id = "123",
        memberCount = 5,
        name = "General",
        type = "messaging",
        image = "https://example.com/img.png",
    )

    val expectedOptionalFieldsMissing = ChannelInfo(
        cid = null,
        id = null,
        memberCount = 0,
        name = null,
        type = null,
        image = null,
    )
}
