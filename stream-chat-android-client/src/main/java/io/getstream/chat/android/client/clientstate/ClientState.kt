package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.utils.State

private typealias UserModel = io.getstream.chat.android.client.models.User

internal sealed class ClientState : State {
    object Idle : ClientState()
    sealed class Anonymous : ClientState() {
        sealed class Pending : Anonymous() {
            object WithoutToken : Pending()
            class WithToken(val token: String) : Pending()
        }

        sealed class Authorized(
            val anonymousUser: UserModel,
            val token: String,
            val connectionId: String
        ) : Anonymous() {
            class Connected(connectionId: String, anonymousUser: UserModel, token: String) :
                Authorized(anonymousUser, token, connectionId)

            class Disconnected(connectionId: String, anonymousUser: UserModel, token: String) :
                Authorized(anonymousUser, token, connectionId)
        }
    }

    sealed class User(val user: UserModel) : ClientState() {
        sealed class Pending(user: UserModel) : User(user) {
            class WithToken(user: UserModel, val token: String) : Pending(user)
            class WithoutToken(user: UserModel) : Pending(user)
        }

        sealed class Authorized(user: UserModel, val token: String, val connectionId: String) : User(user) {
            class Connected(connectionId: String, user: UserModel, token: String) :
                Authorized(user, token, connectionId)

            class Disconnected(connectionId: String, user: UserModel, token: String) :
                Authorized(user, token, connectionId)
        }
    }

    internal fun tokenOrError(): String = when (this) {
        is User.Pending.WithToken -> token
        is User.Authorized -> token
        is Anonymous.Pending.WithToken -> token
        is Anonymous.Authorized -> token
        else -> error("This state doesn't contain token!")
    }

    internal fun userOrError(): UserModel = when (this) {
        is User -> user
        is Anonymous.Authorized -> anonymousUser
        else -> error("This state doesn't contain user!")
    }

    internal fun connectionIdOrError(): String = when (this) {
        is User.Authorized -> connectionId
        is Anonymous.Authorized -> connectionId
        else -> error("This state doesn't contain connectionId")
    }
}
