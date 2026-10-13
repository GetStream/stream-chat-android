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
    fun `each orientation puts every corner where the EXIF tag says`() {
        // Corners of the stored image: top-left, top-right, bottom-left, bottom-right.
        val expected = mapOf(
            ExifInterface.ORIENTATION_NORMAL to listOf(Red, Green, Blue, White),
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL to listOf(Green, Red, White, Blue),
            ExifInterface.ORIENTATION_ROTATE_180 to listOf(White, Blue, Green, Red),
            ExifInterface.ORIENTATION_FLIP_VERTICAL to listOf(Blue, White, Red, Green),
            // Mirrored across the top-left to bottom-right diagonal.
            ExifInterface.ORIENTATION_TRANSPOSE to listOf(Red, Blue, Green, White),
            ExifInterface.ORIENTATION_ROTATE_90 to listOf(Blue, Red, White, Green),
            // Mirrored across the top-right to bottom-left diagonal.
            ExifInterface.ORIENTATION_TRANSVERSE to listOf(White, Green, Blue, Red),
            ExifInterface.ORIENTATION_ROTATE_270 to listOf(Green, White, Red, Blue),
        )

        expected.forEach { (orientation, corners) ->
            val bitmap = decode(jpeg(width = 60, height = 30, orientation = orientation), targetSize = 100)
            val quarterTurn = orientation in QuarterTurns

            assertEquals("width for orientation $orientation", if (quarterTurn) 30 else 60, bitmap.width)
            assertEquals("height for orientation $orientation", if (quarterTurn) 60 else 30, bitmap.height)
            assertEquals("corners for orientation $orientation", corners, corners(bitmap))
        }
    }

    private fun decode(uri: Uri, targetSize: Int): Bitmap {
        val bitmap = decodeSampledBitmap(context.contentResolver, uri, targetSize)
        assertNotNull(bitmap)
        return bitmap!!
    }

    // Each quarter of the stored image has its own color, so every flip and turn can be told apart.
    private fun jpeg(width: Int, height: Int, orientation: Int? = null): Uri {
        val source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val left = x < width / 2
                val top = y < height / 2
                source.setPixel(
                    x,
                    y,
                    when {
                        top && left -> Color.RED
                        top -> Color.GREEN
                        left -> Color.BLUE
                        else -> Color.WHITE
                    },
                )
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

    // The color at the center of each quarter: top-left, top-right, bottom-left, bottom-right.
    private fun corners(bitmap: Bitmap): List<String> {
        val (w, h) = bitmap.width to bitmap.height
        return listOf(w / 4 to h / 4, w * 3 / 4 to h / 4, w / 4 to h * 3 / 4, w * 3 / 4 to h * 3 / 4)
            .map { (x, y) -> colorName(bitmap.getPixel(x, y)) }
    }

    private fun colorName(pixel: Int): String {
        val bright = { channel: Int -> channel > Bright }
        val (r, g, b) = Triple(bright(Color.red(pixel)), bright(Color.green(pixel)), bright(Color.blue(pixel)))
        return when {
            r && g && b -> White
            r -> Red
            g -> Green
            b -> Blue
            else -> "#${Integer.toHexString(pixel)}"
        }
    }

    private companion object {
        const val JpegQuality = 100
        const val Bright = 200
        const val Red = "red"
        const val Green = "green"
        const val Blue = "blue"
        const val White = "white"

        val QuarterTurns = setOf(
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSVERSE,
            ExifInterface.ORIENTATION_ROTATE_270,
        )
    }
}
