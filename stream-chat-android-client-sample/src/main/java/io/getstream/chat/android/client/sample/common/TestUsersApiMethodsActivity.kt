package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.sample.App
import io.getstream.chat.android.client.sample.R
import io.getstream.chat.android.client.utils.FilterObject
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiAddMembersBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiAnonymousBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiBanUserBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiFlagUserBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiFunctionalityGroup
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiGuestBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiLoadingShapeContainer
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiLoginGroup
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiMuteUserBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiQueryUsersBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiRemoveMembersBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiSetRegularUserBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiUnBanUserBtn
import kotlinx.android.synthetic.main.activity_test_user_api.testUserApiUnMuteUserBtn

class TestUsersApiMethodsActivity : AppCompatActivity() {

    private val client = App.client
    private val channelId = "new-ch"
    private val channelType = "messaging"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test_user_api)

        initViews()
    }

    override fun onDestroy() {
        client.disconnect()
        super.onDestroy()
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
        // testUserApiLoadingShapeContainer.makeVisible()

        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"

        client.getGuestToken("id", "name").enqueue {

            if (it.isSuccess) {
                val user = it.data().user
                client.setUser(user, token)
            }
        }
    }

    private fun setAnonymousUser() {
        // testUserApiLoadingShapeContainer.makeVisible()
        client.setAnonymousUser()
    }

    private fun setRegularUser() {
        // testUserApiLoadingShapeContainer.makeVisible()

        client.subscribe {
            if (it is ConnectedEvent) {
                registerDevice()
                initButtons()
            }
        }

        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYmVuZGVyIn0.3KYJIoYvSPgTURznP8nWvsA2Yj2-vLqrm-ubqAeOlcQ"

        client.setUser(User("bender"), token)
    }

    private fun registerDevice() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.token?.let { firebaseToken ->
                    client.addDevice(firebaseToken).enqueue { result ->
                        if (result.isSuccess) {
                            // User device registered success
                        } else {
                            // Device not registered
                        }
                    }
                }
            }
        }
    }

    private fun getUsers() {
        client.queryUsers(getQueryUserRequest()).enqueue { result ->

            if (result.isSuccess) {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
            } else {
                result.error().cause?.printStackTrace()
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

            // echoResult(result, "Users gets successful")
        }
    }

    private fun addMembers() {
        client.addMembers(
            channelId = channelId,
            channelType = channelType,
            members = listOf("stream-eugene")
        ).enqueue { result ->
            // echoResult(result, "Member added successful")
        }
    }

    private fun removeMembers() {
        client.removeMembers(
            channelId = channelId,
            channelType = channelType,
            members = listOf("stream-eugene")
        ).enqueue { result ->
            // echoResult(result, "Member removed successful")
        }
    }

    private fun muteUser() {
        client.muteUser("stream-eugene").enqueue { result ->
            // echoResult(result, "Member muted successful")
        }
    }

    private fun unMuteUser() {
        client.unmuteUser("stream-eugene").enqueue { result ->
            // echoResult(result, "Member unmuted successful")
        }
    }

    private fun flag() {
        client.flagUser("stream-eugene").enqueue { result ->
            // echoResult(result, "Flag successful")
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
            // echoResult(result, "User baned successful")
        }
    }

    private fun unBanUser() {
        client.unBanUser(
            targetId = "stream-eugene",
            channelType = channelType,
            channelId = channelId
        ).enqueue { result ->
            // echoResult(result, "User unbaned successful")
        }
    }

    private fun getQueryUserRequest(): QueryUsersRequest {
        val filter = FilterObject("type", "messaging")
        val sort = QuerySort<User>()
            .asc(User::lastActive)
        // return QueryUsers(0, 10, filter, sort)
        return QueryUsersRequest(filter, 0, 10)
    }

    private fun initButtons() {
        testUserApiFunctionalityGroup?.visibility = View.VISIBLE
        testUserApiLoginGroup?.visibility = View.GONE

        testUserApiLoadingShapeContainer.visibility = View.GONE
    }
}
