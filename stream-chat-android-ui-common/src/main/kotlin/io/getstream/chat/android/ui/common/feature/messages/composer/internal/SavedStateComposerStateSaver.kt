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

package io.getstream.chat.android.ui.common.feature.messages.composer.internal

import androidx.lifecycle.SavedStateHandle
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.log.StreamLog

/**
 * [ComposerStateSaver] implementation backed by [SavedStateHandle].
 *
 * Attachments are only saved when all extra data values are parcel-safe; otherwise
 * saving is skipped entirely to avoid crashes during [android.app.Activity.onSaveInstanceState].
 */
@InternalStreamChatApi
public class SavedStateComposerStateSaver(
    private val savedStateHandle: SavedStateHandle,
) : ComposerStateSaver {

    private val logger = StreamLog.getLogger("Chat:ComposerStateSaver")

    override fun saveAttachments(attachments: List<Attachment>) {
        if (!attachments.areExtraDataParcelSafe()) {
            logger.w {
                "[saveAttachments] Skipping attachment save: extraData contains non-parcelable values. " +
                    "Attachments will not survive process death."
            }
            savedStateHandle.remove<ArrayList<ParcelableAttachment>>(KEY_ATTACHMENTS)
            return
        }
        savedStateHandle[KEY_ATTACHMENTS] = ArrayList(attachments.map { it.toParcelable() })
    }

    override fun restoreAttachments(): List<Attachment>? {
        return savedStateHandle.get<ArrayList<ParcelableAttachment>>(KEY_ATTACHMENTS)
            ?.map { it.toAttachment() }
    }

    override fun clear() {
        savedStateHandle.remove<ArrayList<ParcelableAttachment>>(KEY_ATTACHMENTS)
    }

    private companion object {
        private const val KEY_ATTACHMENTS = "io.getstream.chat.composer.attachments"
    }
}
