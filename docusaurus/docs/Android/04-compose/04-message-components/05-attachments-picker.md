# AttachmentsPicker

The `AttachmentsPicker` component allows users to pick media, file and media capture attachments to send to the chat. The picker is a **bound component**, that loads all the data and prepares it for the user.

Internally, it sets up the following components:

* Picker options: The header in the picker that shows different types of attachment options people can choose from — media, files and media capture, as well as the **Send** button.
* Image picker: Shows a gallery of images to choose from the device.
* File picker: Shows a list of files to choose from the device. Also allows the user to open the file browser and pick more files from the system.
* Media capture: Shows the media capture option to the user and opens a media capture `Activity`.

The picker also handles required permissions for browsing files and capturing images.

Let's see how to use it.

## Usage

If you're using **screen** components, like the [`MessagesScreen`](./01-messages-screen.md), you don't have to do any setup for the `AttachmentsPicker`. If you're building custom screens, you can add the `AttachmentPicker` to the rest of your UI:

```kotlin
// The state if we need to show the picker or not
val isShowingAttachments = attachmentsPickerViewModel.isShowingAttachments
        
if (isShowingAttachments) {
    AttachmentsPicker( // Add the picker to your UI
        attachmentsPickerViewModel = attachmentsPickerViewModel,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .height(350.dp),
        onAttachmentsSelected = { attachments ->
            // Handle selected attachments
        },
        onDismiss = {
            // Handle dismiss
        }
    )
}
```

Because the `AttachmentsPicker` is a **bound** component, you should rely on the `AttachmentsPickerViewModel`'s state, to know if you should show the picker or not. To make sure the picker is shown only when it should be, wrap the component call in an `if` statement.

The code above will show the following UI:

 ![Default AttachmentsPicker component](../../assets/compose_default_attachments_picker_component.png)

As you can see, the picker is really easy to add to the UI, and it lets your users choose from different types of attachments when sending their messages.

## Handling Actions

The `AttachmentsPicker` exposes these actions for customization, as per the signature:

```kotlin
@Composable
fun AttachmentsPicker(
    onAttachmentsSelected: (List<Attachment>) -> Unit,
    onDismiss: () -> Unit,
)
```

* `onAttachmentsSelected`: Handler when attachments are selected and confirmed, by clicking on the **Send** button.
* `onDismiss`: Handler when the picker is dismissed, by clicking outside of the picker area.

To customize the behavior of the picker, simply override these actions to fit your UI needs, like in the following example:

```kotlin
AttachmentsPicker(
    ..., // State and UI customization
    onAttachmentsSelected = { attachments -> 
        // Dismiss the picker and store the attachments
        attachmentsPickerViewModel.onShowAttachments(false)
        composerViewModel.onAttachmentsSelected(attachments)
    },
    onDismiss = { // Reset the UI state and dismiss the picker
        attachmentsPickerViewModel.onShowAttachments(false)
        attachmentsPickerViewModel.onDismiss()
    }
)
```

In the example above, when `onAttachmentsSelected()` is triggered, you call `onShowAttachments()` and dismiss the picker from the UI, but you also store the selected attachments in the `MessageComposerViewModel`, so that you can show them in the `MessageInput` component.

Alternatively, in `onDismiss()`, you hide the picker again and you call `attachmentsPickerViewModel.onDismiss()` to reset the picker state, such as the loaded files or images and the `AttachmentPickerMode`.

This is a very simple way to customize the behavior and connect the `AttachmentPicker` to the rest of your components.

## Customization

You can customize the `AttachmentsPicker` with its `modifier` parameter:

* `modifier`: Allows you to customize the root content component of the picker and change its shape, size padding and more.
