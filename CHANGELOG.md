# To be released:

- Significant performance improvements to offline storage
- Default message limit for the queryChannels use case changed from 10 to 1. This is a more sensible default for the channel list view of most chat apps
- Update LLC to version 1.16.6
- Use `createdLocallyAt` and `updatedLocallyAt` properties in ChannelController and ThreadController
- Set attachment fields even if the file upload fails

# Oct 7th, 2020 - 0.8.4
- Update client to 1.16.5: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.5

# Oct 5th, 2020 - 0.8.3
- Improved message attachment handling. Message is now first added to local storage and the attachment is uploaded afterwards.
- Editing messages now works while offline
- Deprecate SendMessageWithAttachments in favor of SendMessage while specifying attachment.upload
- Fix a bug that caused messages not to load if member limit wasn't specified
- Fix a crash related to reaction data structure
- Fix a bug where network errors (temporary ones) are detected as permanent errors

# Oct 2nd, 2020 - 0.8.2
- Request members by default when querying channels

# Sep 30th, 2020 - 0.8.1
- Handle the new `ChannelUpdatedByUserEvent` 
- Update client to 1.16.1: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.1
- Improve online status handling
- Replace posting an empty channels map when the channels query wasn't run online and offline storage is empty with error

# Sep 23rd, 2020 - 0.8.0
- Update client to 1.16.0: See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.16.0

# Sep 23rd, 2020 - 0.7.7
- Fix crash when map channels DB entity to Channel
- Add posting empty channels map when queryChannels fails either offline and online which prevents infinite loader

# Sep 18th, 2020 - 0.7.6
- Store needed users in DB
- Stop trying to execute background sync in case ChatDomain.offlineEnabled is set to false
- Fix Socket Connection/Reconnection
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.5

# Sep 15th, 2020 - 0.7.5
- Fix offline support for adding and removing reactions
- Fix crash when creating a channel while channel.createdBy is not set

# Sep 14th, 2020 - 0.7.4
- Remove duplicates of new channels
- Improve tests
- Remove some message's properties that are not used anymore GetStream/stream-chat-android-client#69
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.4

# Sep 8th, 2020 - 0.7.3
- Add usecase to send Giphy command
- Add usecase to shuffle a Gif on Giphy command message
- Add usecase to cancel Giphy Command
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.3

# Aug 28th, 2020 - 0.7.2
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.1

# Aug 28th, 2020 - 0.7.1
- Keep order when retry to send a message
- Fix message sync logic and message sending success event emitting
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.15.0

# Aug 20th, 2020 - 0.7.0
- Update to version 0.7.0

# Aug 3nd, 2020 - 0.6.9
- Fix `QuerySort`

# Jul 20th, 2020 - 0.6.8
- Fix `NotificationAddedToChannelEvent` event handling

# Jul 3nd, 2020 - 0.6.7
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.10.0
- Implement Thread Behavior

# Jun 24th, 2020 - 0.6.6
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.9.2

# Jun 16th, 2020 - 0.6.5
- Fixed crash caused by `NotificationMarkReadEvent.user` value being sent null.
- Solution: using the current user which was set to the ChatDomain instead of relying on event's data.

# Jun 12th, 2020 - 0.6.4
- Add attachment.type when upload a file or image

# Jun 11th, 2020 - 0.6.3
- Create a new UseCase to send messages with attachments

# Jun 11th, 2020 - 0.6.2
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.6.1

# May 30th, 2020 - 0.6.1
- Use the new low level client syntax for creating a channel with members
- Fallback to a default channel config if the real channel config isn't available yet. This fixes GetStream/stream-chat-android#486

# May 27th, 2020 - 0.6.0
- Update client to the latest version: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.5.0

# May 26th, 2020 - 0.5.2
- Test cases for notification removed from channel had the wrong data structure. This caused a crash when this event was triggered.

# May 25th, 2020 - 0.5.1
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.4.16

# May 24th, 2020 - 0.5.0
Livedata now supports all events exposed by the chat API. The 3 new events are:
- Channel truncated
- Notification channel truncated
- Channel Deleted
This release also improves how new channels are created.

# May 23rd, 2020 - 0.4.8
- NotificationMessageNew doesn't specify event.message.cid, this was causing issues with offline storage. The test suite has been updated and the issue is now resolved. Also see: GetStream/stream-chat-android#490

# May 23rd, 2020 - 0.4.7
- Fixed NPE on MemberRemoved event GetStream/stream-chat-android#476
- Updates low level client to fix GetStream/stream-chat-android#492

# May 20th, 2020 - 0.4.6
- Update client to the latest version. See changes: https://github.com/GetStream/stream-chat-android-client/releases/tag/1.4.11

# May 16th, 2020 - 0.4.5
- Improved handling of unread counts. Fixes GetStream/stream-chat-android#475

# May 16th, 2020 - 0.4.4
- GetStream/stream-chat-android#476

# May 15th, 2020 - 0.4.3
- Resolves this ticket: GetStream/stream-chat-android#479

# May 13th, 2020 - 0.4.2
-NotificationAddedToChannelEvent cid parsing didn't work correctly. This has been fixed in 0.4.2

# May 13th, 2020 - 0.4.1
- There was an issue with the 0.4.0 and the data structure for NotificationMarkRead

# May 13th, 2020 - 0.4.0
## Features:
- Massive improvement to javadoc/dokka
- Support for user ban events. Exposed via chatDomain.banned
- Muted users are available via chatDomain.muted
- Support for notificationMarkRead, invite and removed from channel events
- Support for deleting channels
- Support for silent messages
- Creating channels with both members and additional data works now
- User presence is enabled

##Bugfixes:
- No longer denormalizing channelData.lastMessageAt
- Fixed an issue with channel event handling and the usage of channel.id vs channel.cid
- Changed channelData.createdBy from lateinit to a regular field

##Other:
- Moved from Travis to Github actions

# May 8th, 2020 - 0.3.4
- added support for muting users
- store the current user in offline storage
- performance tests
- removed launcher icons from lib
- forward compatibility with new event sync endpoint
- support for reaction scores

# May 2nd, 2020 - 0.3.3

# May 2nd, 2020 - 0.3.1
- Make the channel unread counts easily accessible via channel.unreadCount
- Support for muting users
- Detection for permanent vs temporary errors (which helps improve retry logic)
- Bugfix: Fixes edge cases where recovery flow runs before the existing API calls complete

# Apr 29th, 2020 - 0.3.0
- Handle edge cases where events are received out of order
- KTlint, travis and coverage reporting
- Interfaces for use cases and controllers for easier testing
- Channel data to isolate channel data vs rest of channel state
- Java version of code examples
- Handle edge cases for channels with more than 100 members
- Test coverage on mark read
- Bugfix queryChannelsController returning duplicate channels
- Support for hiding and showing channels
- Full offline pagination support (including the difference between GTE and GT filters)

# Apr 22nd, 2020 - 0.2.1
- Better handling for missing cids

# Apr 22nd, 2020 - 0.2.0
- Test suite > 100 tests
- Sample app (stream-chat-android) works
- Full offline sync for channels, messages and reactions
- Easy to use livedata objects for building your own UI

# Apr 22nd, 2020 - 0.1.0
- First Release
