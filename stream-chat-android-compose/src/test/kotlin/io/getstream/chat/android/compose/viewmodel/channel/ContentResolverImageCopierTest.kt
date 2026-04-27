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

package io.getstream.chat.android.compose.viewmodel.channel

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ContentResolverImageCopierTest {

    private lateinit var context: Context
    private lateinit var copier: ContentResolverImageCopier

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        copier = ContentResolverImageCopier(context)
    }

    @Test
    fun `copyToCache copies bytes from file uri into timestamped cache`() = runTest {
        val bytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val source = File(context.cacheDir, "gallery_src_${randomString()}.jpg")
        source.writeBytes(bytes)
        val uri = Uri.fromFile(source)

        val result = copier.copyToCache(uri)

        assertNotNull(result)
        assertTrue(result!!.exists())
        assertArrayEquals(bytes, result.readBytes())
        source.delete()
        result.parentFile?.deleteRecursively()
    }

    @Test
    fun `copyToCache returns null when uri cannot be opened`() = runTest {
        val result = copier.copyToCache(Uri.parse("content://${randomString()}/missing"))
        assertNull(result)
    }
}
