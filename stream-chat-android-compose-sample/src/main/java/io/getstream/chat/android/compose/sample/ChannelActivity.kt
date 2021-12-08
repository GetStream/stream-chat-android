package io.getstream.chat.android.compose.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.channel.list.ChannelItemState
import io.getstream.chat.android.compose.ui.channel.ChannelsScreen
import io.getstream.chat.android.compose.ui.channel.header.ChannelListHeader
import io.getstream.chat.android.compose.ui.channel.info.ChannelInfo
import io.getstream.chat.android.compose.ui.channel.list.ChannelList
import io.getstream.chat.android.compose.ui.channel.list.DefaultChannelItem
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelViewModelFactory
import io.getstream.chat.android.offline.ChatDomain

class ChannelActivity : AppCompatActivity() {

    private val factory by lazy {
        ChannelViewModelFactory(
            ChatClient.instance(), ChatDomain.instance(),
            QuerySort.desc("last_updated"),
            Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()?.id ?: ""))
            )
        )
    }

    private val listViewModel: ChannelListViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * To use the Compose SDK/Components, simply call [setContent] to provide a Compose UI
         * definition, in which you gain access to all the UI component functions.
         *
         * You can use the default [ChannelsScreen] component that sets everything up for you,
         * or build a custom component yourself, like [MyCustomUi].
         */
        setContent {
            ChatTheme(dateFormatter = ChatApp.dateFormatter) {
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

    /**
     * An example of a screen UI that's much more simple than the ChannelsScreen component, that features a custom
     * ChannelList item.
     */
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    private fun MyCustomUiSimplified() {
        val user by ChatDomain.instance().user.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            ChannelListHeader(
                title = stringResource(id = R.string.app_name),
                currentUser = user
            )

            ChannelList(
                itemContent = {
                    CustomChannelListItem(channelItem = it, user = user)
                },
                divider = {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(0.5.dp)
                            .align(CenterHorizontally)
                            .background(color = ChatTheme.colors.textLowEmphasis)
                    )
                },
            )
        }
    }

    /**
     * An example of a customized DefaultChannelItem component.
     */
    @Composable
    private fun CustomChannelListItem(channelItem: ChannelItemState, user: User?) {
        DefaultChannelItem(
            channelItem = channelItem,
            currentUser = user,
            onChannelLongClick = { },
            onChannelClick = { },
            trailingContent = {
                Spacer(modifier = Modifier.width(8.dp))
            },
            detailsContent = {
                Text(
                    text = ChatTheme.channelNameFormatter.formatChannelName(it.channel),
                    style = ChatTheme.typography.bodyBold,
                    color = ChatTheme.colors.textHighEmphasis
                )
            }
        )
    }

    /**
     * An example of what a custom UI can be, when not using [ChannelsScreen].
     *
     * It's important to note that if we want to use the [ChannelInfo] to expose information and
     * options that the user can make with each channel, we need to use a [Box] and overlap the
     * two elements. This makes it easier as it's all presented in the same layer, rather than being
     * wrapped in drawers or more components.
     */
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    private fun MyCustomUi() {
        var query by remember { mutableStateOf("") }

        val user by listViewModel.user.collectAsState()
        val selectedChannel by remember { listViewModel.selectedChannel }
        val connectionState by listViewModel.connectionState.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChannelListHeader(
                    title = stringResource(id = R.string.app_name),
                    currentUser = user,
                    connectionState = connectionState
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
                    modifier = Modifier.fillMaxSize(),
                    viewModel = listViewModel,
                    onChannelClick = ::openMessages,
                    onChannelLongClick = { listViewModel.selectChannel(it) }
                )
            }

            val currentSelectedChannel = selectedChannel
            if (currentSelectedChannel != null) {
                ChannelInfo(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    isMuted = listViewModel.isChannelMuted(currentSelectedChannel.cid),
                    selectedChannel = currentSelectedChannel,
                    currentUser = user,
                    onChannelOptionClick = { action -> listViewModel.performChannelAction(action) }
                )
            }
        }
    }

    private fun openMessages(channel: Channel) {
        startActivity(MessagesActivity.getIntent(this, channel.cid))
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ChannelActivity::class.java)
        }
    }
}
