/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.parser2.testdata.AttachmentDtoTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class AttachmentDtoAdapterTest {

    private val parser = MoshiChatParser(
        EventMapping(
            DomainMapping(
                { "" },
                NoOpChannelTransformer,
                NoOpMessageTransformer,
            )
        ),
        DtoMapping(
            NoOpMessageTransformer
        ),
    )

    @Test
    fun `Deserialize JSON attachment with custom fields`() {
        val attachment = parser.fromJson(AttachmentDtoTestData.json, AttachmentDto::class.java)
        attachment shouldBeEqualTo AttachmentDtoTestData.attachment
    }

    @Test
    fun `Deserialize JSON attachment without custom fields`() {
        val attachment = parser.fromJson(AttachmentDtoTestData.jsonWithoutExtraData, AttachmentDto::class.java)
        attachment shouldBeEqualTo AttachmentDtoTestData.attachmentWithoutExtraData
    }

    @Test
    fun `Serialize JSON attachment with custom fields`() {
        val jsonString = parser.toJson(AttachmentDtoTestData.attachment)
        jsonString shouldBeEqualTo AttachmentDtoTestData.json
    }

    @Test
    fun `Serialize JSON attachment without custom fields`() {
        val jsonString = parser.toJson(AttachmentDtoTestData.attachmentWithoutExtraData)
        jsonString shouldBeEqualTo AttachmentDtoTestData.jsonWithoutExtraData
    }
}
