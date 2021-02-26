## Common changes for all artifacts

## stream-chat-android

## stream-chat-android-client

## stream-chat-android-offline
- Deprecated `ChatDomain::disconnect`, use disconnect on ChatClient instead, it will make the disconnection on ChatDomain too.
- Deprecated constructors for `ChatDomain.Builder` with the `User` type parameter, use constructor with `Context` and `ChatClient` instead.

## stream-chat-android-ui-common

## stream-chat-android-ui-components
- Introduced `AttachmentViewFactory` as a factory for custom attachment views/custom link view
- Updated PhotoView to version 2.3.0
