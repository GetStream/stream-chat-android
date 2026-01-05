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

package io.getstream.chat.android.ui.common.feature.documents

import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.randomString
import io.getstream.chat.android.ui.common.R
import org.junit.After
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class AttachmentDocumentActivityTest {

    private val mockChatClient: ChatClient = mock()

    @After
    fun tearDown() {
        reset(mockChatClient)
    }

    @Test
    fun `activity finishes when ChatClient is not initialized`() {
        val intent = AttachmentDocumentActivity.getIntent(ApplicationProvider.getApplicationContext(), randomString())

        ActivityScenario.launch<AttachmentDocumentActivity>(intent).use { scenario ->
            assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        }
    }

    @Test
    fun `activity finishes when ChatClient is not connected to socket`() {
        initializeChatClient()
        whenever(mockChatClient.isSocketConnected()) doReturn false

        val intent = AttachmentDocumentActivity.getIntent(ApplicationProvider.getApplicationContext(), randomString())

        ActivityScenario.launch<AttachmentDocumentActivity>(intent).use { scenario ->
            assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        }
    }

    @Test
    fun `activity loads document when ChatClient is connected to socket`() {
        initializeChatClient()
        whenever(mockChatClient.isSocketConnected()) doReturn true

        val url = randomString()
        val intent = AttachmentDocumentActivity.getIntent(ApplicationProvider.getApplicationContext(), url)

        ActivityScenario.launch<AttachmentDocumentActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val webView = activity.findViewById<WebView>(R.id.webView)
                val shadowWebView = Shadows.shadowOf(webView)
                assertEquals("https://docs.google.com/gview?embedded=true&url=$url", shadowWebView.lastLoadedUrl)
            }
        }
    }

    private fun initializeChatClient() {
        object : ChatClient.ChatClientBuilder() {
            override fun internalBuild(): ChatClient = mockChatClient
        }.build()
    }
}
