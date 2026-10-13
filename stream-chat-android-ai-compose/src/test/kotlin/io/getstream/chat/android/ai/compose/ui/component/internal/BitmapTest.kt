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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal class BitmapTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `a large image is downsampled by a power of two`() {
        val uri = jpeg(width = 400, height = 200)

        val bitmap = decode(uri, targetSize = 100)

        assertEquals(200, bitmap.width)
        assertEquals(100, bitmap.height)
    }

    @Test
    fun `an image smaller than the target keeps its size`() {
        val uri = jpeg(width = 80, height = 40)

        val bitmap = decode(uri, targetSize = 100)

        assertEquals(80, bitmap.width)
        assertEquals(40, bitmap.height)
    }

    @Test
    fun `orientations that turn the image a quarter swap width and height`() {
        listOf(
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE,
        ).forEach { orientation ->
            val bitmap = decode(jpeg(width = 60, height = 30, orientation = orientation), targetSize = 100)

            assertEquals("orientation $orientation", 30, bitmap.width)
            assertEquals("orientation $orientation", 60, bitmap.height)
        }
    }

    @Test
    fun `orientations that keep the image upright keep width and height`() {
        listOf(
            ExifInterface.ORIENTATION_NORMAL,
            ExifInterface.ORIENTATION_ROTATE_180,
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
        ).forEach { orientation ->
            val bitmap = decode(jpeg(width = 60, height = 30, orientation = orientation), targetSize = 100)

            assertEquals("orientation $orientation", 60, bitmap.width)
            assertEquals("orientation $orientation", 30, bitmap.height)
        }
    }

    @Test
    fun `a horizontal flip mirrors the image`() {
        val bitmap = decode(
            jpeg(width = 60, height = 30, orientation = ExifInterface.ORIENTATION_FLIP_HORIZONTAL),
            targetSize = 100,
        )

        assertIsBlue(bitmap.getPixel(5, 15))
        assertIsRed(bitmap.getPixel(54, 15))
    }

    @Test
    fun `a half turn puts the left side on the right`() {
        val bitmap = decode(
            jpeg(width = 60, height = 30, orientation = ExifInterface.ORIENTATION_ROTATE_180),
            targetSize = 100,
        )

        assertIsBlue(bitmap.getPixel(5, 15))
        assertIsRed(bitmap.getPixel(54, 15))
    }

    private fun decode(uri: Uri, targetSize: Int): Bitmap {
        val bitmap = decodeSampledBitmap(context.contentResolver, uri, targetSize)
        assertNotNull(bitmap)
        return bitmap!!
    }

    // The left half is red and the right half blue, so mirrored images can be told apart.
    private fun jpeg(width: Int, height: Int, orientation: Int? = null): Uri {
        val source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                source.setPixel(x, y, if (x < width / 2) Color.RED else Color.BLUE)
            }
        }
        val file = File.createTempFile("image", ".jpg", context.cacheDir)
        file.outputStream().use { source.compress(Bitmap.CompressFormat.JPEG, JpegQuality, it) }
        if (orientation != null) {
            ExifInterface(file).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
        }
        return Uri.fromFile(file)
    }

    private fun assertIsRed(pixel: Int) {
        assertTrue("#${Integer.toHexString(pixel)} is red", Color.red(pixel) > Bright && Color.blue(pixel) < Dark)
    }

    private fun assertIsBlue(pixel: Int) {
        assertTrue("#${Integer.toHexString(pixel)} is blue", Color.blue(pixel) > Bright && Color.red(pixel) < Dark)
    }

    private companion object {
        const val JpegQuality = 100
        const val Bright = 200
        const val Dark = 60
    }
}
