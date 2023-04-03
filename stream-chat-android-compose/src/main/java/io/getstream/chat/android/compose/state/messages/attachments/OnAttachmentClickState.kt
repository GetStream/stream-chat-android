/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.state.messages.attachments

import androidx.activity.compose.ManagedActivityResultLauncher
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.ui.attachments.preview.ImagePreviewContract

// TODO write documentation
public sealed interface OnAttachmentClickState

public class OnUploadAttachmentClickState(public val attachment: Attachment) : OnAttachmentClickState

public class OnLinkAttachmentClickState(public val previewUrl: String) : OnAttachmentClickState

public class OnGiphyAttachmentClickState(public val url: String) : OnAttachmentClickState

public class OnImageAttachmentClickState(
    public val imagePreviewLauncher: ManagedActivityResultLauncher<ImagePreviewContract.Input, ImagePreviewResult?>,
    public val message: Message,
    public val attachmentPosition: Int,
    public val skipEnrichUrl: Boolean,
) : OnAttachmentClickState

public class OnFileAttachmentClickState(public val attachment: Attachment) : OnAttachmentClickState
