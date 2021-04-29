---
id: clientUploadingFiles
title: Uploading Files
sidebar_position: 17
---

The `channel.sendImage` and `channel.sendFile` methods make it easy to upload files.

> This functionality defaults to using the Stream CDN. If you would like, you can easily change the logic to upload to your own CDN of choice.

> The maximum file size is 20mb for the Stream Chat CDN.

## Uploading an Image

```kotlin
val channelClient = client.channel("messaging", "general")

// Upload an image without detailed progress
channelClient.sendImage(imageFile).enqueue { result->
    if (result.isSuccess) {
        // Successful upload, you can now attach this image
        // to an message that you then send to a channel
        val imageUrl = result.data()
        val attachment = Attachment(
            type = "image",
            imageUrl = imageUrl,
        )
        val message = Message(
            attachments = mutableListOf(attachment),
        )
        channelClient.sendMessage(message).enqueue { /* ... */ }
    }
}
```

> **NOTE**: Attachments need to be linked to the message after the upload is completed.

## Uploading a File

```kotlin
// Upload a file, monitoring for progress with a ProgressCallback
channelClient.sendFile(anyOtherFile, object : ProgressCallback {
    override fun onSuccess(file: String) {
        val fileUrl = file
    }

    override fun onError(error: ChatError) {
        // Handle error
    }

    override fun onProgress(progress: Long) {
        // You can render the uploading progress here
    }
}).enqueue() // No callback passed to enqueue, as we'll get notified above anyway
```

## Customizing Upload Logic

You can use your own CDN. You'll have to create your own implementation of the FileUploader interface, and any upload and delete calls will be sent to that implementation.

The code examples below show how to change where files are uploaded:

```kotlin
// Set a custom FileUploader implementation when building your client
val client = ChatClient.Builder("39mr6a3z4tem", context)
    .fileUploader(MyFileUploader())
    .build()
}
```
