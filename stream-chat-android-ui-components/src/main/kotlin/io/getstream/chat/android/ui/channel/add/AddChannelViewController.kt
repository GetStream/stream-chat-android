package io.getstream.chat.android.ui.channel.add

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name

internal class AddChannelViewController(private val headerView: AddChannelHeaderView) {

    private val members: MutableList<User> = mutableListOf()

    init {
        headerView.membersInputListener = object : AddChannelHeaderView.Listener {
            override fun onInputChanged(query: String) {
                // Filter users list
            }

            override fun onMemberAdded(query: String) {
                // Just for testing purposes - will be removed
                if (query.isNotEmpty()) {
                    onMemberAdded()
                    members.add(randomUser(query))
                    headerView.setMembers(members.toList())
                }
            }
        }

        headerView.setAddMemberButtonClickListener {
            addMemberButtonClicked()
        }
    }

    private fun onMemberAdded() {
        headerView.hideInput()
        headerView.showAddMemberButton()
    }

    private fun addMemberButtonClicked() {
        headerView.showInput()
        headerView.hideAddMemberButton()
    }

    // Just for testing purposes - will be removed
    private fun randomImageUrl(): String {
        val category = listOf("men", "women").random()
        val index = (0..99).random()
        return "https://randomuser.me/api/portraits/$category/$index.jpg"
    }

    // Just for testing purposes - will be removed
    private fun randomUser(username: String): User {
        return User().apply {
            name = username
            image = randomImageUrl()
            id = "${('A'..'Z').random()}${('A'..'Z').random()}"
        }
    }
}
