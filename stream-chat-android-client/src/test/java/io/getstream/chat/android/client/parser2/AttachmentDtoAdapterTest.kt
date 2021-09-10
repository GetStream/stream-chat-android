package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.parser2.testdata.AttachmentDtoTestData
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class AttachmentDtoAdapterTest {

    private val parser = MoshiChatParser()

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
