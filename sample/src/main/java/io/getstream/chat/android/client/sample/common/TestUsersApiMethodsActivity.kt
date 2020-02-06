package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import io.getstream.chat.android.client.FilterObject
import io.getstream.chat.android.client.User
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.requests.QuerySort
import io.getstream.chat.android.client.requests.QueryUsers
import io.getstream.chat.android.client.rest.AddDeviceRequest
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
import kotlinx.android.synthetic.main.activity_test_user_api.*

class TestUsersApiMethodsActivity : AppCompatActivity() {

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
        //testUserApiLoadingShapeContainer.makeVisible()

        client.setGuestUser(User("guest_user")).enqueue {

            if (it.isSuccess) {
                val user = it.data().user
                val token = it.data().access_token
                client.setUser(user, token)
            }
        }
    }

    private fun setAnonymousUser() {
        //testUserApiLoadingShapeContainer.makeVisible()
        client.setAnonymousUser()
    }

    private fun setRegularUser() {
        //testUserApiLoadingShapeContainer.makeVisible()

        client.events().subscribe {
            if (it is ConnectedEvent) {
                registerDevice()
                initButtons()
            }
        }

        client.setUser(User("bender"))
    }

    private fun registerDevice() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.token?.let { firebaseToken ->
                     client.addDevice(
                        AddDeviceRequest(firebaseToken)
                    ).enqueue { result ->
                        if (result.isSuccess) {
                            //User device registered success
                        } else {
                            //Device not registered
                        }
                    }
                }
            }
        }
    }

    private fun getUsers() {
        client.getUsers(getQueryUserRequest()).enqueue { result ->
            //echoResult(result, "Users gets successful")
        }
    }

    private fun addMembers() {
        client.addMembers(
            channelId = channelId,
            channelType = channelType,
            members = listOf("stream-eugene")
        ).enqueue { result ->
            //echoResult(result, "Member added successful")
        }
    }

    private fun removeMembers() {
        client.removeMembers(
            channelId = channelId,
            channelType = channelType,
            members = listOf("stream-eugene")
        ).enqueue { result ->
            //echoResult(result, "Member removed successful")
        }
    }

    private fun muteUser() {
        client.muteUser(
            targetId = "stream-eugene"
        ).enqueue { result ->
            //echoResult(result, "Member muted successful")
        }
    }

    private fun unMuteUser() {
        client.unMuteUser(
            targetId = "stream-eugene"
        ).enqueue { result ->
            //echoResult(result, "Member unmuted successful")
        }
    }

    private fun flag() {
        client.flag(
            targetId = "stream-eugene"
        ).enqueue { result ->
            //echoResult(result, "Flag successful")
        }
    }

    private fun banUser() {
        client.banUser(
            targetId = "stream-eugene",
            channelType = channelType,
            channelId = channelId,
            timeout = 10,
            reason = "reason"
        ).enqueue { result ->
            //echoResult(result, "User baned successful")
        }
    }

    private fun unBanUser() {
        client.unBanUser(
            targetId = "stream-eugene",
            channelType = channelType,
            channelId = channelId
        ).enqueue { result ->
            //echoResult(result, "User unbaned successful")
        }
    }

    private fun getQueryUserRequest(): QueryUsers {
        val filter = FilterObject()
        val sort: QuerySort = QuerySort().asc("last_active")
        return QueryUsers(filter, sort).withLimit(10).withOffset(0)
    }

    private fun initButtons() {
        testUserApiFunctionalityGroup?.visibility = View.VISIBLE
        testUserApiLoginGroup?.visibility = View.GONE

        testUserApiLoadingShapeContainer.visibility = View.GONE
    }
}