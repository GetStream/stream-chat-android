package io.getstream.chat.android.client.sample.examples.basic

import android.os.Bundle
import io.getstream.chat.android.client.sample.common.BaseChannelsListFragment

class ChannelsListFragment : BaseChannelsListFragment() {

    override fun reload() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        App.client.queryChannels(
//            ChannelsQuery().apply {
//                this.limit = 10
//                this.offset = 10
//            }
//        )
    }
}
