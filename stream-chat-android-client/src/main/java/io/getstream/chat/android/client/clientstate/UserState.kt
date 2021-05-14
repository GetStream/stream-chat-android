package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.models.User as UserModel

internal sealed class UserState {
    object NotSet : UserState()
    class UserSet(val user: UserModel) : UserState()
    sealed class Anonymous : UserState() {
        object Pending : Anonymous()
        class AnonymousUserSet(val anonymousUser: UserModel) : Anonymous()
    }

    internal fun userOrError(): UserModel = when (this) {
        is UserSet -> user
        is Anonymous.AnonymousUserSet -> anonymousUser
        else -> error("This state doesn't contain user!")
    }
}
