package io.getstream.chat.android.client.clientstate

private typealias UserModel = io.getstream.chat.android.client.models.User

internal sealed class ClientState {
    object Idle : ClientState()
    sealed class Anonymous : ClientState() {
        object Pending : Anonymous()

        sealed class Authorized(
            val anonymousUser: UserModel,
            val connectionId: String
        ) : Anonymous() {
            class Connected(connectionId: String, anonymousUser: UserModel) :
                Authorized(anonymousUser, connectionId)

            class Disconnected(connectionId: String, anonymousUser: UserModel) :
                Authorized(anonymousUser, connectionId)
        }
    }

    sealed class User(val user: UserModel) : ClientState() {
        class Pending(user: UserModel) : User(user)

        sealed class Authorized(user: UserModel, val connectionId: String) : User(user) {
            class Connected(connectionId: String, user: UserModel) :
                Authorized(user, connectionId)

            class Disconnected(connectionId: String, user: UserModel) :
                Authorized(user, connectionId)
        }
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
