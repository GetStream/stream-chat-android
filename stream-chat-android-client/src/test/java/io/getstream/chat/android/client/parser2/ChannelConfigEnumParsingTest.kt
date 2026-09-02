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

package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.parser2.testdata.ChannelDtoTestData
import io.getstream.chat.android.network.models.ChannelConfigWithInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The config's automod, blocklist and push fields used to be plain strings that accepted any value.
 * They are sealed classes now, so a mode the SDK does not know must still reach the domain unchanged
 * rather than being dropped or coerced.
 */
internal class ChannelConfigEnumParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    // Derived from the shared fixture so the required fields stay in one place.
    private val configJsonWithUnknownEnums = ChannelDtoTestData.configJson
        .replace("\"automod\":\"disabled\"", "\"automod\":\"future_mode\"")
        .replace("\"automod_behavior\":\"flag\"", "\"automod_behavior\":\"future_behavior\"")
        .replace("\"blocklist_behavior\":\"block\"", "\"blocklist_behavior\":\"future_blocklist\"")

    @Test
    fun `Unrecognised config modes are preserved as Unknown`() {
        val config = parser.fromJson(configJsonWithUnknownEnums, ChannelConfigWithInfo::class.java)

        assertEquals(ChannelConfigWithInfo.Automod.Unknown("future_mode"), config.automod)
        assertEquals(
            ChannelConfigWithInfo.AutomodBehavior.Unknown("future_behavior"),
            config.automodBehavior,
        )
        assertEquals(
            ChannelConfigWithInfo.BlocklistBehavior.Unknown("future_blocklist"),
            config.blocklistBehavior,
        )
    }

    @Test
    fun `Unrecognised config modes reach the domain as the raw wire value`() {
        val config = parser.fromJson(configJsonWithUnknownEnums, ChannelConfigWithInfo::class.java)

        assertEquals("future_mode", config.automod.value)
        assertEquals("future_behavior", config.automodBehavior.value)
        assertEquals("future_blocklist", config.blocklistBehavior?.value)
    }
}
