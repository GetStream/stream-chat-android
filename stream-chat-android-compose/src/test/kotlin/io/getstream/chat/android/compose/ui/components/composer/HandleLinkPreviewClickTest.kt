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

package io.getstream.chat.android.compose.ui.components.composer

import android.content.Context
import android.content.Intent
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.LinkPreview
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

internal class HandleLinkPreviewClickTest {
    private val context: Context = mock()
    private val linkPreview: LinkPreview = LinkPreview(
        originUrl = "theurl.com",
        attachment = Attachment(
            titleLink = "https://loremipsumdolor.sit",
            title = "Title",
            text = "Text",
            imageUrl = "Image",
        ),
    )

    @Test
    fun `handleLinkPreviewClick should delegate to onClick when available`() {
        val onClick = mock<(LinkPreview) -> Unit>()

        handleLinkPreviewClick(onClick, context, linkPreview)

        verify(onClick).invoke(linkPreview)
    }

    @Test
    fun `handleLinkPreviewClick should start activity when onClick is null`() {
        handleLinkPreviewClick(null, context, linkPreview)

        verify(context).startActivity(
            argThat { intent ->
                intent.action == Intent.ACTION_VIEW &&
                    intent.data.toString() == "https://loremipsumdolor.sit"
            },
        )
    }
}
