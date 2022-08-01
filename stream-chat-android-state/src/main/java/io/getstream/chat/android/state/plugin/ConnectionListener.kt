package io.getstream.chat.android.state.plugin

internal interface ConnectionListener {

    suspend fun onConnectionEstablished(userId: String)

    suspend fun onConnectionLost()

}