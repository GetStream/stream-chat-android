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
