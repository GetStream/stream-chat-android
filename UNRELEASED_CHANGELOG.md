## Common changes for all artifacts

## stream-chat-android

## stream-chat-android-client

## stream-chat-android-offline
- Deprecated `ChatDomain::disconnect`, use disconnect on ChatClient instead, it will make the disconnection on ChatDomain too.

## stream-chat-android-ui-common

## stream-chat-android-ui-components
- Introduced `AttachmentViewFactory` as a factory for custom attachment views/custom link view
- Updated PhotoView to version 2.3.0
- Introduced `TextAndAttachmentsViewHolder` for any combination of attachment content and text
- Deleted `OnlyFileAttachmentsViewHolder`, `OnlyMediaAttachmentsViewHolder`, 
  `PlainTextWithMediaAttachmentsViewHolder` and `PlainTextWithFileAttachmentsViewHolder`
