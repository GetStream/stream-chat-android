package io.getstream.chat.android.livedata.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.livedata.BaseTest
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.repository.toEntity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ListConverterTest : BaseTest() {
    val converter = ListConverter()

    @Test
    fun stringListNull() {
        val output = converter.stringListToString(null)
        val converted = converter.stringToStringList(output)
        Truth.assertThat(converted).isEqualTo(listOf<String>())
    }

    @Test
    fun stringList() {
        val input = listOf("a", "b")
        val output = converter.stringListToString(input)
        val converted = converter.stringToStringList(output)
        Truth.assertThat(converted!!).isEqualTo(input)
    }

    @Test
    fun attachmentListNull() {
        val output = converter.attachmentListToString(null)
        val converted = converter.stringToAttachmentList(output)
        Truth.assertThat(converted).isEqualTo(listOf<Attachment>())
    }

    @Test
    fun attachmentList() {
        val input = listOf(data.attachment1)
        val output = converter.attachmentListToString(input)
        val converted = converter.stringToAttachmentList(output)
        Truth.assertThat(converted).isEqualTo(input)
    }

    @Test
    fun reactionListNull() {
        val output = converter.reactionListToString(null)
        val converted = converter.stringToReactionList(output)
        Truth.assertThat(converted).isEqualTo(listOf<ReactionEntity>())
    }

    @Test
    fun reactionList() {
        val input = listOf(data.reaction1.toEntity())
        val output = converter.reactionListToString(input)
        val converted = converter.stringToReactionList(output)
        Truth.assertThat(converted!!).isEqualTo(input)
    }
}
