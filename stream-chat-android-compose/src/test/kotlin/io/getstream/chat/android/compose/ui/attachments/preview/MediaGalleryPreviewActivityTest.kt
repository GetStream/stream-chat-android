/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.attachments.preview

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.compose.ui.ComposeTest
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.helper.DefaultDownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MediaGalleryPreviewActivityTest : ComposeTest {

    @Test
    fun `launch activity with no crash`() {
        val contract = MediaGalleryPreviewContract()
        val intent = contract.createIntent(
            context = ApplicationProvider.getApplicationContext(),
            input = MediaGalleryPreviewContract.Input(
                message = randomMessage(attachments = listOf(randomAttachment(), randomAttachment())),
                videoThumbnailsEnabled = true,
                downloadAttachmentUriGenerator = DefaultDownloadAttachmentUriGenerator,
                downloadRequestInterceptor = {},
                streamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
            ),
        )

        ActivityScenario.launch<MediaGalleryPreviewActivity>(intent)
    }
}
