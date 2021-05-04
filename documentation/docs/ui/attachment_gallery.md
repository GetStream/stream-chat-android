---
id: uiAttachmentGallery
title: Attachment Gallery
sidebar_position: 6
---

_AttachmentGalleryActivity_ is an _Activity_ used to display attachments that the users have sent in the chat. It is an image gallery where users can see the pictures, share, download, and use a menu to navigate through the pictures.

| Light Mode | Dark Mode |
| --- | --- |
|![attachment_gallery_example1_light](/img/attachment_gallery_example1_light.png)|![attachment_gallery_example1_dark](/img/attachment_gallery_example1_dark.png)|
|![attachment_gallery_example2_light](/img/attachment_gallery_example2_light.png)|![attachment_gallery_example2_dark](/img/attachment_gallery_example2_dark.png)|

## Navigating To Attachment Gallery
By default, the Attachment Gallery is opened when a user clicks on an attachment in _MessageListView_. In that case, all actions mentioned above have a default implementation, which can be changed by overriding _MessageListView_'s handlers.
You can also navigate to _AttachmentGalleryActivity_ but in that case, you will need to implement all available actions:
```kotlin
// Create Attachment Gallery Destination
val destination = AttachmentGalleryDestination(
    requireContext(),
    attachmentReplyOptionHandler = { resultItem ->
        // Handle reply
    },
    attachmentShowInChatOptionHandler = { resultItem ->
        // Handle show image in chat
    },
    attachmentDownloadOptionHandler = { resultItem ->
        // Handle download image
    },
    attachmentDeleteOptionClickHandler = { resultItem ->
        // Handle delete image
    },
)

// Register destination with the ActivityResultRegistry
activity?.activityResultRegistry?.let { registry ->
    destination.register(registry)
}

// Set the data to display
destination.setData(attachmentGalleryItems = listOf(), attachmentIndex = 0)

// Fire the navigation request
ChatUI.instance().navigator.navigate(destination)
```
