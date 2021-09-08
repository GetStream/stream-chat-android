package io.getstream.chat.sample.data.user

/**
 * In-memory storage for currently logged-in User.
 */
class UserRepository(var user: SampleUser = SampleUser.None)
