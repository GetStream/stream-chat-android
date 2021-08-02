package io.getstream.chat.ui.sample.feature.chat

import android.content.Context
import android.content.Intent
import android.widget.Toast
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListActivity
import io.getstream.chat.android.ui.channel.list.ChannelListFragment

class CustomChannelListActivity : ChannelListActivity(), ChannelListFragment.ChannelClickListener {

    override fun createChannelListFragment(): ChannelListFragment {
        return ChannelListFragment.newInstance {
            setFragment(CustomChannelListFragment())
            showSearch(false)
            showHeader(true)
        }
    }

    override fun onChannelClick(channel: Channel) {
        Toast.makeText(this, "Channel clicked", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, CustomChannelListActivity::class.java)
        }
    }
}
