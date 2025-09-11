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

package io.getstream.chat.android.compose.viewmodel.channel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewController
import io.getstream.chat.android.ui.common.utils.AttachmentConstants
import io.getstream.chat.android.ui.common.utils.StreamFileUtil
import io.getstream.chat.android.ui.common.utils.extensions.getDisplayableName
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.common.utils.shareLocalFile
import io.getstream.log.taggedLogger
import io.getstream.result.onErrorSuspend
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ChannelMediaAttachmentsPreviewViewModel(
    private val context: Context,
) : ViewModel() {

    private val logger by taggedLogger("Chat:ChannelMediaAttachmentsPreviewViewModel")

    private val _state = MutableStateFlow(ChannelMediaAttachmentsPreviewViewState())

    /**
     * @see [ChannelAttachmentsViewController.state]
     */
    val state: StateFlow<ChannelMediaAttachmentsPreviewViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChannelMediaAttachmentsPreviewViewEvent>(extraBufferCapacity = 1)

    /**
     * @see [ChannelAttachmentsViewController.events]
     */
    val events: SharedFlow<ChannelMediaAttachmentsPreviewViewEvent> = _events.asSharedFlow()

    private var sharingJob: Job? = null

    /**
     * @see [ChannelAttachmentsViewController.onViewAction]
     */
    fun onViewAction(action: ChannelMediaAttachmentsPreviewViewAction) {
        when (action) {
            is ChannelMediaAttachmentsPreviewViewAction.ShareClick -> if (state.value.isPreparingToShare) {
                cancelSharing()
            } else {
                startSharing(attachment = action.attachment)
            }

            is ChannelMediaAttachmentsPreviewViewAction.ConfirmSharingClick ->
                share(attachment = action.attachment)

            is ChannelMediaAttachmentsPreviewViewAction.DismissSharingClick ->
                dismissSharing()
        }
    }

    private fun startSharing(attachment: Attachment) {
        logger.d { "[startSharing] mimeType: ${attachment.mimeType}, attachment: ${attachment.imagePreviewUrl}" }
        viewModelScope.launch {
            if (attachment.fileSize >= AttachmentConstants.MAX_SIZE_BEFORE_DOWNLOAD_WARNING_IN_BYTES) {
                logger.d {
                    "[startSharing] Attachment larger than " +
                        "${AttachmentConstants.MAX_SIZE_BEFORE_DOWNLOAD_WARNING_IN_BYTES} bytes, checking cache..."
                }
                withContext(DispatcherProvider.IO) {
                    StreamFileUtil.getFileFromCache(context, attachment)
                }.onSuccess { uri ->
                    logger.d { "[startSharing] Attachment found in cache, starting share intent..." }
                    context.shareLocalFile(
                        uri = uri,
                        mimeType = attachment.mimeType,
                        text = attachment.getDisplayableName(),
                    )
                }.onErrorSuspend { error ->
                    logger.e { "[startSharing] Attachment not in cache" }
                    _state.update { currentState ->
                        currentState.copy(promptedAttachment = attachment)
                    }
                }
            } else {
                share(attachment)
            }
        }
    }

    private fun share(attachment: Attachment) {
        logger.d { "[share] mimeType: ${attachment.mimeType}, attachment: ${attachment.imagePreviewUrl}" }
        _state.update { currentState ->
            currentState.copy(
                isPreparingToShare = true,
                promptedAttachment = null,
            )
        }
        sharingJob = viewModelScope.launch {
            withContext(DispatcherProvider.IO) {
                StreamFileUtil.writeFileToShareableFile(context, attachment)
            }.onSuccess { uri ->
                logger.d { "[share] Attachment ready, starting share intent..." }
                context.shareLocalFile(
                    uri = uri,
                    mimeType = attachment.mimeType,
                    text = attachment.getDisplayableName(),
                )
            }.onError { error ->
                logger.e { "[share] failed to share attachment: ${error.message}" }
                _events.tryEmit(ChannelMediaAttachmentsPreviewViewEvent.SharingError(error))
            }
            _state.update { currentState ->
                currentState.copy(isPreparingToShare = false)
            }
        }
    }

    private fun cancelSharing() {
        logger.d { "[cancelSharing] no args" }
        sharingJob?.cancel()
        _state.update { currentState ->
            currentState.copy(isPreparingToShare = false)
        }
    }

    private fun dismissSharing() {
        logger.d { "[dismissSharing] no args" }
        _state.update { currentState ->
            currentState.copy(promptedAttachment = null)
        }
    }
}
