/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.gallery.internal

import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal object AttachmentGalleryRepository {
    private var attachmentGalleryItems: List<AttachmentGalleryItem> = emptyList()
    private var attachmentGalleryItemsObserverCount = 0

    fun getAttachmentGalleryItems(): Flow<List<AttachmentGalleryItem>> = flow {
        emit(attachmentGalleryItems)
    }

    fun setAttachmentGalleryItems(attachmentGalleryItems: List<AttachmentGalleryItem>) {
        AttachmentGalleryRepository.attachmentGalleryItems = attachmentGalleryItems
    }

    fun registerAttachmentGalleryItemsObserver() {
        attachmentGalleryItemsObserverCount++
    }

    fun unregisterAttachmentGalleryItemsObserver() {
        if (--attachmentGalleryItemsObserverCount == 0) {
            attachmentGalleryItems = emptyList()
        }
    }
}
