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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.LinkPreview

@InternalStreamChatApi
public object PreviewLinkData {

    public val link1: LinkPreview = LinkPreview(
        originUrl = "https://www.getstream.io",
        attachment = Attachment(
            title = "Lorem ipsum dolor sit amet",
            text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.",
            type = "image",
            mimeType = "image/jpeg",
            imageUrl = "https://example.com/image1.jpg",
        ),
    )
}
