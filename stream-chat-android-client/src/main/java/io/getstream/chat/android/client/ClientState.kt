package io.getstream.chat.android.client

import io.getstream.chat.android.client.models.User

internal sealed class ChatClientState {
    object Idle : ChatClientState()
    sealed class AnonymousUserState : ChatClientState() {
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

    sealed class UserState(val user: User) : ChatClientState() {
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

internal fun onConnected(
    state: ChatClientState,
    user: User,
    connectionId: String
): ChatClientState {
    return when (state) {
        is ChatClientState.UserState.AuthorizationPending.AuthorizationPendingWithToken ->
            ChatClientState.UserState.UserAuthorized.Connected(connectionId, user, state.token)
        is ChatClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithToken ->
            ChatClientState.AnonymousUserState.AnonymousUserAuthorized.AnonymousUserConnected(
                connectionId,
                user,
                state.token
            )
        else -> state.inappropriateStateError("be connected")
    }
}

internal fun onDisconnected(state: ChatClientState): ChatClientState {
    return when (state) {
        is ChatClientState.Idle -> ChatClientState.Idle
        is ChatClientState.UserState.UserAuthorized.Connected -> ChatClientState.UserState.UserAuthorized.Disconnected(
            state.connectionId,
            state.user,
            state.token
        )
        is ChatClientState.AnonymousUserState.AnonymousUserAuthorized.AnonymousUserConnected -> ChatClientState.AnonymousUserState.AnonymousUserAuthorized.AnonymousUserDisconnected(
            state.connectionId,
            state.anonymousUser,
            state.token
        )
        else -> state.inappropriateStateError("be disconnected")
    }
}

internal fun onTokenReceived(state: ChatClientState, token: String): ChatClientState {
    return when (state) {
        is ChatClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithoutToken ->
            ChatClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithToken(
                token
            )
        is ChatClientState.UserState.AuthorizationPending.AuthorizationPendingWithoutToken ->
            ChatClientState.UserState.AuthorizationPending.AuthorizationPendingWithToken(
                state.user,
                token
            )
        else -> state.inappropriateStateError("receive token")
    }
}

internal fun onSetUser(state: ChatClientState, user: User): ChatClientState {
    return when (state) {
        is ChatClientState.Idle -> ChatClientState.UserState.AuthorizationPending.AuthorizationPendingWithoutToken(
            user
        )
        else -> state.inappropriateStateError("set user")
    }
}

internal fun onSetAnonymousUser(state: ChatClientState): ChatClientState {
    return when (state) {
        is ChatClientState.Idle -> ChatClientState.AnonymousUserState.AnonymousUserPending.AnonymousPendingWithoutToken
        else -> state.inappropriateStateError("set anonymous user")
    }
}

internal fun ChatClientState.inappropriateStateError(actionName: String): Nothing =
    error("Cannot $actionName while being in inappropriate state $this")
