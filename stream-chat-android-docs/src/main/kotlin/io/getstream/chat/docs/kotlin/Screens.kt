package io.getstream.chat.docs.kotlin

import android.content.Context
import androidx.annotation.StyleRes
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.ChannelListActivity
import io.getstream.chat.android.ui.channel.ChannelListFragment
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.message.MessageListActivity
import io.getstream.chat.android.ui.message.MessageListFragment

class Screens {

    class ChannelList {

        fun channelListActivity(context: Context) {
            context.startActivity(ChannelListActivity.createIntent(context))
        }

        fun channelListFragment(@StyleRes themeResId: Int) {
            ChannelListFragment.newInstance {
                customTheme(themeResId)
                showSearch(false)
                showHeader(false)
            }
        }
    }

    class CustomChannelList {

        class CustomChannelListActivity : ChannelListActivity(), ChannelListFragment.HeaderUserAvatarClickListener {

            override fun createChannelListFragment(): ChannelListFragment {
                return ChannelListFragment.newInstance {
                    setFragment(CustomChannelListFragment())
                    showSearch(false)
                    showHeader(false)
                }
            }

            override fun onUserAvatarClick() {
                // Handle avatar click
            }
        }

        class CustomChannelListFragment : ChannelListFragment() {

            override fun setupChannelListHeader() {
                super.setupChannelListHeader()
                binding.channelListHeaderView.setOnUserAvatarClickListener {
                    // Handle avatar click
                }
            }

            override fun getFilter(): FilterObject? {
                // Return custom filter
                return null
            }

            override fun getSort(): QuerySort<Channel> {
                // Return custom sort
                return ChannelListViewModel.DEFAULT_SORT
            }
        }
    }

    class MessageList {

        fun messageListActivity(context: Context, cid: String, messageId: String?) {
            context.startActivity(MessageListActivity.createIntent(context, cid, messageId))
        }

        fun messageListFragment(cid: String, messageId: String?, @StyleRes themeResId: Int) {
            MessageListFragment.newInstance(cid) {
                customTheme(themeResId)
                showHeader(false)
                messageId(messageId)
            }
        }
    }

    class CustomMessageList {

        class CustomMessageListActivity : MessageListActivity(), MessageListFragment.BackPressListener {

            override fun createMessageListFragment(cid: String, messageId: String?): MessageListFragment {
                return MessageListFragment.newInstance(cid) {
                    setFragment(CustomMessageListFragment())
                    showHeader(true)
                    messageId(messageId)
                }
            }

            override fun onBackPress() {
                // Handle back press
            }
        }

        class CustomMessageListFragment : MessageListFragment() {

            override fun setupMessageListHeader() {
                super.setupMessageListHeader()
                binding.messageListHeaderView.setBackButtonClickListener {
                    // Handle back press
                }
            }
        }
    }
}
