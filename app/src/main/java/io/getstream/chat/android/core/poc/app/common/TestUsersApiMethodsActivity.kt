package io.getstream.chat.android.core.poc.app.common

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.extensions.*
import io.getstream.chat.android.core.poc.library.Result
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
    private val channelId = "general"
    private val channelType = "team"

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
    }

    private fun setGuestUser() {
        testUserApiLoadingShapeContainer.makeVisible()
        client.setGuestUser(User("guest_user")) { result ->
            echoResult(result, "Guest user set up successful")

            testUserApiFunctionalityGroup?.makeVisibleIf(result.isSuccess, View.GONE)
            testUserApiLoginGroup?.makeGoneIf(result.isSuccess)
        }
    }

    private fun setAnonymousUser() {
        testUserApiLoadingShapeContainer.makeVisible()
        client.setAnonymousUser { result ->
            echoResult(result, "Guest user set up successful")
            initButtons(result)
        }
    }

    private fun setRegularUser() {
        testUserApiLoadingShapeContainer.makeVisible()
        client.setUser(User("bender"), object : TokenProvider {
            override fun getToken(listener: TokenProvider.TokenProviderListener) {
                listener.onSuccess("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ")
            }
        }) {
            echoResult(it, "Connected", "Socket connection error")
            initButtons(it)
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
            members = listOf("bender")
        ).enqueue { result ->
            echoResult(result, "Member added successful")
        }
    }

    private fun removeMembers() {
        client.removeMembers(
            channelId = channelId,
            channelType = channelType,
            members = listOf("bender")
        ).enqueue { result ->
            echoResult(result, "Member removed successful")
        }
    }

    private fun muteUser() {
        client.muteUser(
            channelId = channelId,
            channelType = channelType,
            targetId = "bender"
        ).enqueue { result ->
            echoResult(result, "Member removed successful")
        }
    }

    private fun getQueryUserRequest(): QueryUsers {
        val filter = FilterObject()
        val sort: QuerySort = QuerySort().asc("last_active")
        return QueryUsers(filter, sort).withLimit(10)
    }

    private fun initButtons(result: Result<*>) {
        testUserApiFunctionalityGroup?.makeVisibleIf(result.isSuccess, View.GONE)
        testUserApiLoginGroup?.makeGoneIf(result.isSuccess)

        testUserApiLoadingShapeContainer.makeGone()
    }
}