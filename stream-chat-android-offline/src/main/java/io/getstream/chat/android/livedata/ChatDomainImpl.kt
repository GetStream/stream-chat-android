package io.getstream.chat.android.livedata

/**
 * The Chat Domain exposes livedata objects to make it easier to build your chat UI.
 * It intercepts the various low level events to ensure data stays in sync.
 * Offline storage is handled using Room
 *
 * A different Room database is used for different users. That's why it's mandatory to specify the user id when
 * initializing the ChatRepository
 *
 * chatDomain.channel(type, id) returns a controller object with channel specific livedata objects
 * chatDomain.queryChannels(query) returns a livedata object for the specific queryChannels query
 *
 * chatDomain.online livedata object indicates if you're online or not
 * chatDomain.totalUnreadCount livedata object returns the current unread count for this user
 * chatDomain.muted the list of muted users
 * chatDomain.banned if the current user is banned or not
 * chatDomain.channelUnreadCount livedata object returns the number of unread channels for this user
 * chatDomain.errorEvents events for errors that happen while interacting with the chat
 *
 */
internal class ChatDomainImpl internal constructor() : ChatDomain
