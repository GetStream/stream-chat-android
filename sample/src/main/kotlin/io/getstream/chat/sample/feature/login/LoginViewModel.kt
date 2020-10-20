package io.getstream.chat.sample.feature.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.Chat
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.sample.R
import io.getstream.chat.sample.application.SampleNotificationHandler
import io.getstream.chat.sample.common.image
import io.getstream.chat.sample.common.name
import io.getstream.chat.sample.data.user.User
import io.getstream.chat.sample.data.user.UserRepository
import timber.log.Timber
import io.getstream.chat.android.client.models.User as ChatUser

class LoginViewModel(
    private val applicationContext: Context,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    fun loginButtonClicked(credentials: LoginCredentials) {
        _state.postValue(State.Loading)
        initChatSdk(credentials)
        initChatUser(credentials)
    }

    fun targetChannelDataReceived(cid: String) {
        val user = userRepository.user
        if (userRepository.user != User.None) {
            val chatUser = ChatUser().apply {
                id = user.id
                image = user.image
                name = user.name
            }
            initChatUser(chatUser, user.token, cid)
        }
    }

    private fun initChatSdk(credentials: LoginCredentials) {
        Chat.Builder(credentials.apiKey, applicationContext).apply {
            offlineEnabled = true
            val notificationConfig =
                NotificationConfig(
                    firebaseMessageIdKey = "message_id",
                    firebaseChannelIdKey = "channel_id",
                    firebaseChannelTypeKey = "channel_type",
                    smallIcon = R.drawable.ic_chat_bubble
                )
            notificationHandler = SampleNotificationHandler(applicationContext, notificationConfig)
        }.build()
    }

    private fun initChatUser(loginCredentials: LoginCredentials) {
        val chatUser = ChatUser().apply {
            id = loginCredentials.userId
            name = loginCredentials.userName
        }
        initChatUser(chatUser, loginCredentials.userToken)
    }

    private fun initChatUser(chatUser: ChatUser, token: String, cid: String? = null) {
        Chat.getInstance()
            .setUser(
                chatUser,
                token,
                object : InitConnectionListener() {
                    override fun onSuccess(data: ConnectionData) {
                        if (cid != null) {
                            _state.postValue(State.RedirectToChannel(cid))
                        } else {
                            _state.postValue(State.RedirectToChannels)
                        }
                        Timber.d("User set successfully")
                    }

                    override fun onError(error: ChatError) {
                        _state.postValue(State.Error(error.message))
                        Timber.e("Failed to set user $error")
                    }
                }
            )
    }
}

sealed class State {
    object RedirectToChannels : State()
    object Loading : State()
    data class RedirectToChannel(val cid: String) : State()
    data class Error(val errorMessage: String?) : State()
}

data class LoginCredentials(
    val apiKey: String,
    val userId: String,
    val userToken: String,
    val userName: String
)
