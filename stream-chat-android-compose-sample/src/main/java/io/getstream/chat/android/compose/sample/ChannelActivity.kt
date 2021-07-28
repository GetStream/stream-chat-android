package io.getstream.chat.android.compose.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.channel.ChannelsScreen
import io.getstream.chat.android.compose.ui.channel.header.ChannelListHeader
import io.getstream.chat.android.compose.ui.channel.info.ChannelInfo
import io.getstream.chat.android.compose.ui.channel.list.ChannelList
import io.getstream.chat.android.compose.ui.common.SearchInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelViewModelFactory
import io.getstream.chat.android.offline.ChatDomain

class ChannelActivity : AppCompatActivity() {

    private val factory by lazy {
        ChannelViewModelFactory(
            ChatClient.instance(), ChatDomain.instance(),
            QuerySort.desc("id"),
            Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: ""))
            )
        )
    }

    private val listViewModel: ChannelListViewModel by viewModels { factory }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * To use the Compose SDK/Components, simply call [setContent] to provide a Compose UI
         * definition, in which you gain access to all the UI component functions.
         *
         * You can use the default [ChannelsScreen] component that sets everything up for you,
         * or build a custom component yourself, like [MyCustomUi].
         * */
        setContent {
            ChatTheme {
                ChannelsScreen(
                    title = stringResource(id = R.string.app_name),
                    isShowingHeader = true,
                    isShowingSearch = true,
                    onItemClick = {
                        startActivity(MessagesActivity.getIntent(this, it.cid))
                    },
                    onBackPressed = { finish() }
                )

//                MyCustomUiSimplified()
//                MyCustomUi()
            }
        }
    }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    fun MyCustomUiSimplified() {
        val user by ChatDomain.instance().user.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            ChannelListHeader(
                title = stringResource(id = R.string.app_name),
                currentUser = user
            )

            ChannelList()
        }
    }

    /**
     * An example of what a custom UI can be, when not using [ChannelsScreen].
     *
     * It's important to note that if we want to use the [ChannelInfo] to expose information and
     * options that the user can make with each channel, we need to use a [Box] and overlap the
     * two elements. This makes it easier as it's all presented in the same layer, rather than being
     * wrapped in drawers or more components.
     * */
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    fun MyCustomUi() {
        var query by remember { mutableStateOf("") }

        val user by listViewModel.user.collectAsState()
        val selectedChannel = listViewModel.selectedChannel
        val isNetworkAvailable by listViewModel.isOnline.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChannelListHeader(
                    title = stringResource(id = R.string.app_name),
                    currentUser = user ?: User(),
                    isNetworkAvailable = isNetworkAvailable
                )

                SearchInput(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    query = query,
                    onValueChange = {
                        query = it
                        listViewModel.setSearchQuery(it)
                    }
                )

                ChannelList(
                    viewModel = listViewModel,
                    onChannelClick = ::openMessages,
                    onChannelLongClick = { listViewModel.selectChannel(it) }
                )
            }

            if (selectedChannel != null) {
                ChannelInfo(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    selectedChannel = selectedChannel,
                    user = user,
                    onChannelOptionClick = { action -> listViewModel.performChannelAction(action) }
                )
            }
        }
    }

    private fun openMessages(channel: Channel) {
        startActivity(MessagesActivity.getIntent(this, channel.cid))
    }
}
