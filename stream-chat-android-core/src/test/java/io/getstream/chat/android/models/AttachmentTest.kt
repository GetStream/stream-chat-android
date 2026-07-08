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

package io.getstream.chat.android.models

import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class AttachmentTest {

    @Test
    @Suppress("LongMethod")
    fun `builder should set every field`() {
        val expected = Attachment(
            authorName = randomString(),
            authorLink = randomString(),
            titleLink = randomString(),
            thumbUrl = randomString(),
            imageUrl = randomString(),
            assetUrl = randomString(),
            ogUrl = randomString(),
            mimeType = randomString(),
            fileSize = randomInt(),
            title = randomString(),
            text = randomString(),
            type = randomString(),
            image = randomString(),
            name = randomString(),
            fallback = randomString(),
            originalHeight = randomInt(),
            originalWidth = randomInt(),
            upload = File(randomString()),
            uploadState = Attachment.UploadState.Success,
            extraData = mapOf(randomString() to randomString()),
        )

        val built = Attachment.Builder()
            .withAuthorName(expected.authorName)
            .withAuthorLink(expected.authorLink)
            .withTitleLink(expected.titleLink)
            .withThumbUrl(expected.thumbUrl)
            .withImageUrl(expected.imageUrl)
            .withAssetUrl(expected.assetUrl)
            .withOgUrl(expected.ogUrl)
            .withMimeType(expected.mimeType)
            .withFileSize(expected.fileSize)
            .withTitle(expected.title)
            .withText(expected.text)
            .withType(expected.type)
            .withImage(expected.image)
            .withName(expected.name)
            .withFallback(expected.fallback)
            .withOriginalHeight(expected.originalHeight)
            .withOriginalWidth(expected.originalWidth)
            .withUpload(expected.upload)
            .withUploadState(expected.uploadState)
            .withExtraData(expected.extraData)
            .build()

        assertEquals(expected, built)
    }

    @Test
    fun `builder copy constructor should copy every field`() {
        val attachment = randomAttachment()

        val built = Attachment.Builder(attachment).build()

        assertEquals(attachment, built)
    }

    @Test
    fun `toString should shorten long urls`() {
        val attachment = Attachment(mimeType = "image/png", thumbUrl = "0123456789abcdef")
        val string = attachment.toString()
        assertTrue(string.contains("mimeType=\"image/png\""))
        assertTrue(string.contains("thumbUrl=0123456789..."))
    }

    @Test
    fun `toString should keep short urls`() {
        val attachment = Attachment(thumbUrl = "short")
        assertTrue(attachment.toString().contains("thumbUrl=short"))
    }
}
