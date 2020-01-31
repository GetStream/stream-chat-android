package io.getstream.chat.android.core.poc.app.common

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.extensions.echoResult
import io.getstream.chat.android.core.poc.extensions.makeGone
import io.getstream.chat.android.core.poc.extensions.makeVisible
import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.FilterObject
import io.getstream.chat.android.core.poc.library.TokenProvider
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.requests.QuerySort
import io.getstream.chat.android.core.poc.library.requests.QueryUsers
import kotlinx.android.synthetic.main.activity_test_user_api.*
import org.jetbrains.anko.intentFor

class TestUsersApiMethodsActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = context.intentFor<TestUsersApiMethodsActivity>()
    }

    private val client = App.client
    private val channelId = "new-ch"
    private val channelType = "messaging"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_user_api)

        initViews()
    }

    private fun initViews() {
        testUserApiGuestBtn?.setOnClickListener {
            setGuestUser()
        }
        testUserApiAnonymousBtn?.setOnClickListener {
            setAnonymousUser()
        }
        testUserApiSetRegularUserBtn?.setOnClickListener {
            setRegularUser()
        }
        testUserApiQueryUsersBtn?.setOnClickListener {
            getUsers()
        }
        testUserApiAddMembersBtn?.setOnClickListener {
            addMembers()
        }
        testUserApiRemoveMembersBtn?.setOnClickListener {
            removeMembers()
        }
        testUserApiMuteUserBtn?.setOnClickListener {
            muteUser()
        }
        testUserApiUnMuteUserBtn?.setOnClickListener {
            unMuteUser()
        }
        testUserApiFlagUserBtn?.setOnClickListener {
            flag()
        }
        testUserApiBanUserBtn?.setOnClickListener {
            banUser()
        }
        testUserApiUnBanUserBtn?.setOnClickListener {
            unBanUser()
        }
    }

    private fun setGuestUser() {
        testUserApiLoadingShapeContainer.makeVisible()

        client.setGuestUser(User("guest_user"))?.subscribe {
            if (it.type == EventType.CONNECTION_SOCKET_OPEN.label) {
                initButtons()
            }
        }
    }

    private fun setAnonymousUser() {
        testUserApiLoadingShapeContainer.makeVisible()
        client.setAnonymousUser().subscribe {
            if (it.type == EventType.CONNECTION_SOCKET_OPEN.label) {
                initButtons()
            }
        }
    }

    private fun setRegularUser() {
        testUserApiLoadingShapeContainer.makeVisible()

        client.setUser(User("stream-eugene"), object : TokenProvider {
            override fun getToken(listener: TokenProvider.TokenProviderListener) {
                listener.onSuccess("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoic3RyZWFtLWV1Z2VuZSJ9.-WNauu6xV56sHM39ZrhxDeBiKjA972O5AYo-dVXva6I")
            }
        }).subscribe {
            if (it.type == EventType.CONNECTION_SOCKET_OPEN.label) {
                initButtons()
            }
        }
    }

    private fun getUsers() {
        client.getUsers(getQueryUserRequest()).enqueue { result ->
            echoResult(result, "Users gets successful")
        }
    }

    private fun addMembers() {
        client.addMembers(
            channelId = channelId,
            channelType = channelType,
            members = listOf("stream-eugene")
        ).enqueue { result ->
            echoResult(result, "Member added successful")
        }
    }

    private fun removeMembers() {
        client.removeMembers(
            channelId = channelId,
            channelType = channelType,
            members = listOf("stream-eugene")
        ).enqueue { result ->
            echoResult(result, "Member removed successful")
        }
    }

    private fun muteUser() {
        client.muteUser(
            targetId = "stream-eugene"
        ).enqueue { result ->
            echoResult(result, "Member muted successful")
        }
    }

    private fun unMuteUser() {
        client.unMuteUser(
            targetId = "stream-eugene"
        ).enqueue { result ->
            echoResult(result, "Member unmuted successful")
        }
    }

    private fun flag() {
        client.flag(
            targetId = "stream-eugene"
        ).enqueue { result ->
            echoResult(result, "Flag successful")
        }
    }

    private fun banUser() {
        client.banUser(
            targetId = "stream-eugene",
            channelType = channelType,
            channelId = channelId,
            timeout = 10
        ).enqueue { result ->
            echoResult(result, "User baned successful")
        }
    }

    private fun unBanUser() {
        client.unBanUser(
            targetId = "stream-eugene",
            channelType = channelType,
            channelId = channelId
        ).enqueue { result ->
            echoResult(result, "User unbaned successful")
        }
    }

    private fun getQueryUserRequest(): QueryUsers {
        val filter = FilterObject()
        val sort: QuerySort = QuerySort().asc("last_active")
        return QueryUsers(filter, sort).withLimit(10).withOffset(0)
    }

    private fun initButtons() {
        testUserApiFunctionalityGroup?.makeVisible()
        testUserApiLoginGroup?.makeGone()

        testUserApiLoadingShapeContainer.makeGone()
    }
}