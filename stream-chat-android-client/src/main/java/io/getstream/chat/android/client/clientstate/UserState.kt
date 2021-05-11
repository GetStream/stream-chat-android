package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.models.User as UserModel

internal sealed class UserState {
    object NotSet : UserState()
    sealed class User(val user: UserModel) : UserState() {
        class Pending(user: UserModel) : User(user)
        class UserSet(user: UserModel) : User(user)
    }
    sealed class Anonymous : UserState() {
        object Pending : Anonymous()
        class AnonymousUserSet(val anonymousUser: UserModel) : Anonymous()
    }

    internal fun userOrError(): UserModel = when (this) {
        is User -> user
        is Anonymous.AnonymousUserSet -> anonymousUser
        else -> error("This state doesn't contain user!")
    }
}
