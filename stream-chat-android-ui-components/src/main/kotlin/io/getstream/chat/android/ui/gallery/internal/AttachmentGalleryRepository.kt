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
