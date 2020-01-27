package io.getstream.chat.android.core.poc.app.common

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.poc.R
import io.getstream.chat.android.core.poc.app.App
import io.getstream.chat.android.core.poc.extensions.echoResult
import io.getstream.chat.android.core.poc.extensions.makeGoneIf
import io.getstream.chat.android.core.poc.extensions.makeVisibleIf
import io.getstream.chat.android.core.poc.library.FilterObject
import io.getstream.chat.android.core.poc.library.User
import io.getstream.chat.android.core.poc.library.requests.QuerySort
import io.getstream.chat.android.core.poc.library.requests.QueryUsers
import kotlinx.android.synthetic.main.activity_test_user_api.*
import org.jetbrains.anko.intentFor

class TestUsersApiMethodsActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = context.intentFor<TestUsersApiMethodsActivity>()
    }

    val client = App.client

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
        testUserApiQueryUsersBtn?.setOnClickListener {
            getUsers()
        }
    }

    private fun setGuestUser() {
        client.setGuestUser(User("guest_user")) { result ->
            echoResult(result, "Guest user set up successful")

            testUserApiFunctionalityGroup?.makeVisibleIf(result.isSuccess, View.GONE)
            testUserApiLoginGroup?.makeGoneIf(result.isSuccess)
        }
    }

    private fun setAnonymousUser() {
        client.setAnonymousUser { result ->
            echoResult(result, "Guest user set up successful")

            testUserApiFunctionalityGroup?.makeVisibleIf(result.isSuccess, View.GONE)
            testUserApiLoginGroup?.makeGoneIf(result.isSuccess)
        }
    }

    private fun getUsers() {
        client.getUsers(getQueryUserRequest()).enqueue { result ->
            echoResult(result, "Guest user set up successful")
        }
    }

    private fun getQueryUserRequest(): QueryUsers {
        val filter = FilterObject()
        val sort: QuerySort = QuerySort().asc("last_active")
        return QueryUsers(filter, sort).withLimit(10)
    }
}