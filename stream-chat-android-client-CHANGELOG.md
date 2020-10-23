# To be released:
- Fix ConcurrentModificationException in `ChatEventsObservable`
- Fix Anonymous and Guest user authentication

# 1.16.8 - Fri 16th of Oct 2020
- Add `lastUpdated` property to `Channel`

# 1.16.7 - Wed 14th of Oct 2020
- Removed many internal implementation classes and methods from the SDK's public API
- Improved nullability, restricted many generic type parameters to be non-nullable (set `Any` as their upper bound)
- Use AttachmentsHelper to validate imageUrl instead of just url.

# 1.16.6 - Fri 9th of Oct 2020
- Add `createdLocallyAt` and `updatedLocallyAt` properties to `Message` type
- Add AttachmentsHelper with hasValidUrl method

# 1.16.5 - Wed 7th of Oct 2020
- Add autocomplete filter
- Add @JvmOverloads to QueryUsersRequest constructor
- Improve java interop of `TokenManager`

# 1.16.4 - Mon 5th of Oct 2020

- Add `attachment.upload` and `attachment.uploadState` fields for livedata upload status. These fields are currently unused if you only use the low level client.

# 1.16.2 - Wed 30 Sep 2020
- Add `ChatClient::enableSlowMode` method to enable slow mode
- Add `ChatClient::disableSlowMode` method to disable slow mode
- Add `ChannelController::enableSlowMode` method to enable slow mode
- Add `ChannelController::disableSlowMode` method to disable slow mode
- Add `Channel::cooldown` property to know how configured `cooldown` time for the channel
- Fix FirebaseMessageParserImpl.verifyPayload() logic
- Fix notification display condition
- Fix Socket connection issues

# 1.16.1 - Wed 25 Sep 2020
- Remove `User` field on `ChannelUpdatedEvent`
- Add new chat event type -> `ChannelUpdatedByUserEvent`
- Add `ChatNotificationHandler::getFirebaseInstanceId` method to provide a custom `FirebaseInstanceId`
- Add `NotificationConfig::useProvidedFirebaseInstance` conf

# 1.16.0 - Wed 23 Sep 2020

- Removed message.channel, this is a backwards incompatible change
- Ensure that message.cid is always available

The SDK was providing message.cid and message.channel in some cases, but not always.
Code that relied on those fields being populated caused bugs in production.

If you were relying on message.channel it's likely that you were running into bugs.
We recommend using one of these alternatives:

- message.cid if you just need a reference to the channel
- the channel object provided by client.queryChannel(s) if you need the full channel data
- channelController.channelData livedata object provided by the livedata package (automatically updated if channel data changes)
- channelController.toChannel() function provided by the livedata package

# 1.15.6 - Wed 23 Sep 2020

- Convert ChatError to plain class. Changes in ChatLogger interface.
- Update events fields related to read status - remove "unread_messages" field and add "unread_channels" to NewMessageEvent, NotificationMarkReadEvent, and NotificationMessageNewEvent
- Mark ChatEvents containing the user property by the UserEvent interface.
- Simplified the event handling APIs, deprecated `ChatObservable`. See [the migration guide](https://github.com/GetStream/stream-chat-android-client/wiki/Migrating-from-the-old-event-subscription-APIs) for details on how to easily adopt the new APIs.

# 1.15.5 - Fri 18 Sep 2020

- Fix Socket Connection/Reconnection

# 1.15.4 - Fri 11 Sep 2020

- Fix Socket Disconnection
- Remove useless message's properties (isStartDay, isYesterday, isToday, date, time and commandInfo)
- Forbid setting new user when previous one wasn't disconnected

# 1.15.3 - Tue 7 Sep 2020

- Add send action operation to ChannelController
- Fix serialized file names of SendActionRequest
- Fix `ConnectedEvent` parse process

# 1.15.2 - Tue 1 Sep 2020

- `ChannelResponse.watchers` is an array of User now
- `Watcher` model has been removed, `User` model should be used instead
- `QueryChannelsRequet` has a new field called `memberLimit` to limit the number of members received per channel

# 1.15.1 - Thu 28 Aug 2020

- New MapAdapter that omit key that contains null values or emptyMaps
- Null-Check over Watchers response

# 1.15.0 - Thu 20 Aug 2020

- Refactor ChatEvents Structure

# 1.14.0 - Thu 20 Aug 2020

- Decouple cloud messages handler logic from configuration data
- Fix createChannel methods

# 1.13.3 - Tue 18 Aug 2020

- Set message as optional when updating a channel

# 1.13.2 - Fri 14 Aug 2020

- Reduce TLS Latency

# 1.13.1 - Fri 7 Aug 2020

- Fix DateParser

# 1.13.0 - Tue 28 Jul 2020

- Add `Client.flagUser()` method to flag an User
- Add `Client.flagMessage()` method to flag a Message
- Deprecated method `Client.flag()` because was a bit confusing, you should use `client.flagUser()` instead

# 1.12.3 - Mon 27 Jul 2020

- Fix NPE on TokenManagerImpl
- Upgrade Kotlin to version 1.3.72
- Add Kotlin Proguard Rules

# 1.12.2 - Fri 17 Jul 2020

- Add customer proguard rules

# 1.12.1 - Wed 15 Jul 2020

- Add customer proguard rules

# 1.12.0 - Mon 06 Jul 2020

- Add mute and unmute methods to channel controller

# 1.11.0 - Mon 06 Jul 2020

- Fix message mentioned users

# 1.10.0 - Wed 29 June 2020

- Add mute and unmute channels
- Add `notification.channel_mutes_updated` socket even handling
- Add user.channelMutes field
- Improve error logging
- Add invalid date format handling (channel.config dates might be invalid)

# 1.9.3 - Wed 29 June 2020

- Add raw socket events logging. See with tag `Chat:Events`

# 1.9.2 - Wed 24 June 2020

- Add `show_in_channel` attribute to `Message` entity

# 1.9.1 - Mue 23 June 2020

- Fix multithreaded date parsing

# 1.9.0 - Mon 22 June 2020

- Fix search message request body
🚨 Breaking change:
- client.searchMessages signature has been changed: query removed, added channel filter

# 1.8.1 - Thu 18 June 2020

- Fix UTC date for sync endpoint
- Fix inhered events parsing
- Fix custom url setter of ChatClient.Builder

# 1.8.0 - Thu 12 June 2020

- Add sync api call

# 1.7.0 - Thu 12 June 2020

- Add query members call

# 1.6.1 - Thu 11 June 2020

- Add MimeType on sendFile and sendImage methods

# 1.6.0 - Mon 8 June 2020

- Add translations api call and update message with `i18n` field. Helper `Message` extensions functions are added.

# 1.5.4 - Wed 3 June 2020

- Add optional `userId` parameter to `Channel.getUnreadMessagesCount` to filter out unread messages for the user

# 1.5.3 - Wed 3 June 2020

- Fix switching users issue: `disconnect` and `setUser` resulted in wrong user connection

# 1.5.2 - Tue 2 June 2020

- Fix `ConcurrentModificationException` on multithread access to socket listeners

# 1.5.1 - Wed 27 May 2020

- Add filter contains with any value

# 1.5.0 - Mon 26 May 2020

🚨 Breaking change:
- Add new constructor field to `Channel`: `team`
- Add new constructor field to `User`: `teams`

✅ Other changes:
- Add `Filter.contains`

# 1.4.17 - Mon 26 May 2020

- Fix loop on client.create
- Fix crash when backend sends first event without me

# 1.4.16 - Mon 25 May 2020

Breaking change:
- `Command` fields are mandatory and marked as non-nullable


# 1.4.15 - Fri 22 May 2020

- Add events: `ChannelTruncated`, `NotificationChannelTruncated`, `NotificationChannelDeleted`

# 1.4.13 - Fri 22 May 2020

🚨 Breaking change:
- Fields `role` and `isInvited` of ``Member` fields optional

# 1.4.12 - Fri 22 May 2020

🚨 Breaking change:
- `Member` model is cleaned up from non existing fields

# 1.4.11 - Tue 19 May 2020

🚨 Breaking change:
- `markRead` of ``ChatClient` and `ChannelController` return `Unit` instead of `ChatEvent`

✅ Other changes:
- Fix null fields which are not marked as nullable

# 1.4.10 - Tue 19 May 2020

- Fix add member invalid api key

# 1.4.9 - Mon 18 May 2020

🚨 Breaking change:
- `markRead` of ``ChatClient` and `ChannelController` return `Unit` instead of `ChatEvent`

✅ Other changes:
- Fix `ChannelController.markRead`: was marking read all channels instead of current one
- `ChatClient.markRead` accepts optional `messageId`

# 1.4.8 - Mon 18 May 2020

- Add handling invalid event payload

# 1.4.7 - Tue 14 May 2020

- Add more channel creation signatures to `Client` and `ChannelController`

# 1.4.6 - Tue 14 May 2020

- Move channel out of message constructor

# 1.4.5 - Tue 12 May 2020

- add message.silent field
- add extension properties `name` and `image` to `Channel` and `User`

# 1.4.3 - Thu 7 May 2020

- fix type erasure of parsed collections: `LinkedTreeMap`, but not `List<Reaction>`

# 1.4.2 - Mon 4 May 2020

- add `reactionScores` to `Message`
- fix null write crash of CustomObject nullable field
- fix extraData duplicated fields

# 1.4.0 - Fri 1 May 2020

- fix `QueryChannelRequest` when `withMessages/withMembers` is called, but messages were not returned
- add `unreadMessages` to `ChannelUserRead`. Add extension for channel to count total unread messages: `channel.getUnreadMessagesCount()`

# 1.3.0 - Wed 30 Apr 2020

🚨 Breaking changes:
- `TokenProvider` signature enforces async execution
- make socket related classes internal

✅ Other changes
- fix endlessly hanging request in case setUser is not called
- fix expired token case on socket connection
- fix client crash if TokenProvider throws an exception

# 1.2.2 - Wed 29 Apr 2020

🚨 Breaking changes:
- fields of models are moved to constructors: `io.getstream.chat.android.client.models`
- field of Device `push_provider` renamed to `pushProvider` and moved to constructor

✅ Other changes
- added local error codes with descriptions: `io.getstream.chat.android.client.errors.ChatErrorCode`
- fix uncaught java.lang.ExceptionInInitializerError while parsing custom object
