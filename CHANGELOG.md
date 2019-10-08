## Pending release

- Fixed serialization of custom fields for message/user/channel and attachment types
- Added support for distinct channels
- Added support to Channel hide/show 
- Improved client error reporting (we now return a parsed error response when available)
- General improvements to Message Input View
- Added ReactionViewClickListener
- Added support for banning and unbanning users
- Added support for deleting a channel
- Add support for switching users via `client.disconnect` and `client.setUser`
- Add `reload` method to `ChannelListViewModel`
- Bugfix: hides attachment drawer after deny permission
- Add support for update channel endpoint

#### Breaking changes:

- `MessageInputView#progressCapturedMedia(int requestCode, int resultCode, Intent data)` renamed into `captureMedia(int requestCode, int resultCode, Intent data)`

## September 28, 2019 - 2.1.0

- Improved support for regenerating expired tokens

## September 28, 2019 - 2.0.1

- Fix channel list ordering when a channel is added directly from Android
- Better Proguard support

## September 26, 2019 - 2.0.0

- Simplify random access to channels
- Channel query and watch methods now work the same as they do on all other SDKs

#### Breaking changes:

- `channel.query` does not watch the channel anymore, to retrieve channel state and watch use `channel.watch`
- `client.getChannelByCID` is now private, use one of the `client.channel` methods to get the same (no null checks needed)
