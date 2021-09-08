package com.getstream.sdk.chat

import com.getstream.sdk.chat.utils.MediaStringUtil.convertFileSizeByteCount
import com.getstream.sdk.chat.utils.MediaStringUtil.convertVideoLength
import com.getstream.sdk.chat.utils.StringUtility.convertMentionedText
import com.getstream.sdk.chat.utils.StringUtility.getDeletedOrMentionedText
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class GetDeletedOrMentionedTextTest {

    @Test
    fun getMentionedMarkDownTextTest() {
        val message = Message(text = "@Steep moon @Broken waterfall hi, there?")

        val user1 = User(
            id = "steep-moon-9",
            extraData = mutableMapOf("name" to "Steep moon")
        )

        val user2 = User(
            id = "broken-waterfall-5",
            extraData = mutableMapOf("name" to "Broken waterfall")
        )

        message.mentionedUsers.add(user1)
        message.mentionedUsers.add(user2)

        val expectedMessage = "**@Steep moon** **@Broken waterfall** hi, there?"
        getDeletedOrMentionedText(message) shouldBeEqualTo expectedMessage
    }

    @Test
    fun getMentionedMarkDownTextWithoutPrefixWhitespaceTest() {
        val message = Message(text = "HI@Steep moonThere")

        val user = User(
            id = "steep-moon-9",
            extraData = mutableMapOf("name" to "Steep moon")
        )

        message.mentionedUsers.add(user)

        val expectedMessage = "HI **@Steep moon**There"
        getDeletedOrMentionedText(message) shouldBeEqualTo expectedMessage
    }

    @Test
    fun newLineTest() {
        val message = Message(text = "\n\n\n .a. \n\n\n")
        getDeletedOrMentionedText(message) shouldBeEqualTo " .a. "
    }

    @Test
    fun convertVideoLengthTest() {
        val videoLength: Long = 216844
        convertVideoLength(videoLength) shouldBeEqualTo "60:14:04"
    }

    @Test
    fun convertFileSizeTest() {
        var fileSize: Long = 999
        convertFileSizeByteCount(fileSize) shouldBeEqualTo "999 B"
        fileSize = 110592
        convertFileSizeByteCount(fileSize) shouldBeEqualTo "110.59 KB"
        fileSize = 452984832
        convertFileSizeByteCount(fileSize) shouldBeEqualTo "452.98 MB"
        fileSize = 900000
        convertFileSizeByteCount(fileSize) shouldBeEqualTo "900 KB"
        fileSize = 0
        convertFileSizeByteCount(fileSize) shouldBeEqualTo "0 B"
        fileSize = -100
        convertFileSizeByteCount(fileSize) shouldBeEqualTo "0 B"
    }

    @Test
    fun convertMentionTextTest() {
        var text: String
        val userName = "Adrian"

        text = "@"
        convertMentionedText(text, userName) shouldBeEqualTo "@Adrian"
        text = "@A"
        convertMentionedText(text, userName) shouldBeEqualTo "@Adrian"
        text = "This@"
        convertMentionedText(text, userName) shouldBeEqualTo "This@Adrian"
        text = "This a @A"
        convertMentionedText(text, userName) shouldBeEqualTo "This a @Adrian"
        text = "@@@This a @"
        convertMentionedText(text, userName) shouldBeEqualTo "@@@This a @Adrian"
        text = "@@@This a @@@@"
        convertMentionedText(text, userName) shouldBeEqualTo "@@@This a @@@@Adrian"
        text = "@@@Adrian a @@This is @A"
        convertMentionedText(text, userName) shouldBeEqualTo "@@@Adrian a @@This is @Adrian"
    }
}
