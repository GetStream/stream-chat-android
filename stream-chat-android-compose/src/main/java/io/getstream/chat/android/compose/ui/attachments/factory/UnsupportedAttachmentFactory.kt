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

package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.UnsupportedAttachmentContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * An [AttachmentFactory] that will be used if no other [AttachmentFactory] can handle the attachments.
 */
public object UnsupportedAttachmentFactory : AttachmentFactory(
    type = Type.BuiltIn.UNSUPPORTED,
    canHandle = { true },
    content = @Composable { modifier, _ ->
        UnsupportedAttachmentContent(
            modifier = modifier
                .wrapContentHeight()
                .width(ChatTheme.dimens.attachmentsContentUnsupportedWidth),
        )
    },
)
