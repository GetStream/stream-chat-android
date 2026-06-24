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

package io.getstream.chat.android.compose.ui.util

import androidx.exifinterface.media.ExifInterface
import io.getstream.chat.android.models.Attachment
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.Base64

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Config.NEWEST_SDK])
internal class LocalAttachmentDimensionsResolverExifTest(private val case: Case) {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `resolveDimensions applies EXIF orientation to the resolved image dimensions`() {
        val file = createJpeg(case.orientation)
        val attachment = Attachment(
            type = "image",
            mimeType = "image/jpeg",
            name = "photo.jpg",
        )

        val resolved = LocalAttachmentDimensionsResolver.resolveDimensions(attachment, file)

        assertEquals(case.expectedWidth, resolved.originalWidth)
        assertEquals(case.expectedHeight, resolved.originalHeight)
    }

    private fun createJpeg(orientation: Int): File {
        val file = tempFolder.newFile("image.jpg")
        file.writeBytes(Base64.getDecoder().decode(LANDSCAPE_JPEG_BASE64))
        ExifInterface(file.absolutePath).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
            saveAttributes()
        }
        return file
    }

    internal data class Case(
        val description: String,
        val orientation: Int,
        val expectedWidth: Int,
        val expectedHeight: Int,
    ) {
        override fun toString(): String = description
    }

    internal companion object {
        private const val RAW_WIDTH = 80
        private const val RAW_HEIGHT = 40

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun cases(): List<Case> = listOf(
            // Orientations that preserve the aspect ratio -> dimensions kept as-is.
            Case("normal keeps dimensions", ExifInterface.ORIENTATION_NORMAL, RAW_WIDTH, RAW_HEIGHT),
            Case("flip horizontal keeps dimensions", ExifInterface.ORIENTATION_FLIP_HORIZONTAL, RAW_WIDTH, RAW_HEIGHT),
            Case("rotate 180 keeps dimensions", ExifInterface.ORIENTATION_ROTATE_180, RAW_WIDTH, RAW_HEIGHT),
            // Orientations that swap the aspect ratio -> dimensions swapped.
            Case("rotate 90 swaps dimensions", ExifInterface.ORIENTATION_ROTATE_90, RAW_HEIGHT, RAW_WIDTH),
            Case("rotate 270 swaps dimensions", ExifInterface.ORIENTATION_ROTATE_270, RAW_HEIGHT, RAW_WIDTH),
            Case("transpose swaps dimensions", ExifInterface.ORIENTATION_TRANSPOSE, RAW_HEIGHT, RAW_WIDTH),
            Case("transverse swaps dimensions", ExifInterface.ORIENTATION_TRANSVERSE, RAW_HEIGHT, RAW_WIDTH),
        )

        private const val LANDSCAPE_JPEG_BASE64 =
            "/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0a" +
                "HBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIy" +
                "MjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAAoAFADASIA" +
                "AhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQA" +
                "AAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3" +
                "ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWm" +
                "p6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEA" +
                "AwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSEx" +
                "BhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElK" +
                "U1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3" +
                "uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwDhqKKK" +
                "k+hCiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAoooo" +
                "A//2Q=="
    }
}
