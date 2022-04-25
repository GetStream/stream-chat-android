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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class AttachmentGalleryViewModel : ViewModel() {
    private val _attachmentGalleryItemsLiveData: MutableLiveData<List<AttachmentGalleryItem>> =
        MutableLiveData<List<AttachmentGalleryItem>>()
    val attachmentGalleryItemsLiveData: LiveData<List<AttachmentGalleryItem>> = _attachmentGalleryItemsLiveData

    init {
        AttachmentGalleryRepository.registerAttachmentGalleryItemsObserver()
        viewModelScope.launch {
            AttachmentGalleryRepository.getAttachmentGalleryItems()
                .collect(_attachmentGalleryItemsLiveData::setValue)
        }
    }

    override fun onCleared() {
        AttachmentGalleryRepository.unregisterAttachmentGalleryItemsObserver()
        super.onCleared()
    }
}
