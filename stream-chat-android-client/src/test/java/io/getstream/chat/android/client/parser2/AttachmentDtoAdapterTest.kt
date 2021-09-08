package io.getstream.chat.android.client.parser2

import com.google.common.truth.Truth.assertThat
import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.parser2.testdata.AttachmentDtoTestData
import org.junit.jupiter.api.Test

internal class AttachmentDtoAdapterTest {

    private val parser = MoshiChatParser()

    @Test
    fun `Deserialize JSON attachment with custom fields`() {
        val attachment = parser.fromJson(AttachmentDtoTestData.json, AttachmentDto::class.java)
        assertThat(attachment).isEqualTo(AttachmentDtoTestData.attachment)
    }

    @Test
    fun `Deserialize JSON attachment without custom fields`() {
        val attachment = parser.fromJson(AttachmentDtoTestData.jsonWithoutExtraData, AttachmentDto::class.java)
        assertThat(attachment).isEqualTo(AttachmentDtoTestData.attachmentWithoutExtraData)
    }

    @Test
    fun `Serialize JSON attachment with custom fields`() {
        val jsonString = parser.toJson(AttachmentDtoTestData.attachment)
        assertThat(jsonString).isEqualTo(AttachmentDtoTestData.json)
    }

    @Test
    fun `Serialize JSON attachment without custom fields`() {
        val jsonString = parser.toJson(AttachmentDtoTestData.attachmentWithoutExtraData)
        assertThat(jsonString).isEqualTo(AttachmentDtoTestData.jsonWithoutExtraData)
    }
}
