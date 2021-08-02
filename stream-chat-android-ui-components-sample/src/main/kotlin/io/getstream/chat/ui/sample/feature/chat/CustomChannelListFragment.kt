package io.getstream.chat.ui.sample.feature.chat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.ui.channel.list.ChannelListFragment

class CustomChannelListFragment : ChannelListFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.channelListHeaderView.setOnUserAvatarClickListener {
            Toast.makeText(context, "Avatar clicked", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Customize filter object
     */
    override fun getFilter(): FilterObject? {
        return null
    }
}
