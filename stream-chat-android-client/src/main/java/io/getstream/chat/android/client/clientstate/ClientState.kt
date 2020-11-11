package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.State

internal sealed class ClientState : State {
    object Idle : ClientState()
    sealed class AnonymousUserState : ClientState() {
        sealed class AnonymousUserPending : AnonymousUserState() {
            object AnonymousPendingWithoutToken : AnonymousUserPending()
            class AnonymousPendingWithToken(val token: String) : AnonymousUserPending()
        }

        sealed class AnonymousUserAuthorized(
            val anonymousUser: User,
            val token: String,
            val connectionId: String
        ) : AnonymousUserState() {
            class AnonymousUserConnected(connectionId: String, anonymousUser: User, token: String) :
                AnonymousUserAuthorized(anonymousUser, token, connectionId)

            class AnonymousUserDisconnected(
                connectionId: String,
                anonymousUser: User,
                token: String
            ) : AnonymousUserAuthorized(anonymousUser, token, connectionId)
        }
    }

    sealed class UserState(val user: User) : ClientState() {
        sealed class AuthorizationPending(user: User) : UserState(user) {
            class AuthorizationPendingWithToken(user: User, val token: String) :
                AuthorizationPending(user)

            class AuthorizationPendingWithoutToken(user: User) : AuthorizationPending(user)
        }

        sealed class UserAuthorized(user: User, val token: String, val connectionId: String) :
            UserState(user) {
            class Connected(connectionId: String, user: User, token: String) :
                UserAuthorized(user, token, connectionId)

            class Disconnected(connectionId: String, user: User, token: String) :
                UserAuthorized(user, token, connectionId)
        }
    }

    internal fun tokenOrError(): String = when (this) {
        is UserState.AuthorizationPending.AuthorizationPendingWithToken -> token
        is UserState.UserAuthorized -> token
        is AnonymousUserState.AnonymousUserPending.AnonymousPendingWithToken -> token
        is AnonymousUserState.AnonymousUserAuthorized -> token
        else -> error("This state doesn't contain token!")
    }

    internal fun userOrError(): User = when (this) {
        is UserState -> user
        is AnonymousUserState.AnonymousUserAuthorized -> anonymousUser
        else -> error("This state doesn't contain user!")
    }

    internal fun connectionIdOrError(): String = when (this) {
        is UserState.UserAuthorized -> connectionId
        is AnonymousUserState.AnonymousUserAuthorized -> connectionId
        else -> error("This state doesn't contain connectionId")
    }
}

internal fun ClientState.inappropriateStateError(actionName: String): Nothing =
    error("Cannot $actionName while being in inappropriate state $this")
