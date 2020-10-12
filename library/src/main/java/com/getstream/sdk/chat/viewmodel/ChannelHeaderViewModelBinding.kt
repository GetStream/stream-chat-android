@file:JvmName("ChannelHeaderViewModelBinding")

package com.getstream.sdk.chat.viewmodel

import android.content.Context
import android.text.format.DateUtils
import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.isInLastMinute
import com.getstream.sdk.chat.view.ChannelHeaderView
import io.getstream.chat.android.client.models.Member

@JvmName("bind")
fun ChannelHeaderViewModel.bindView(view: ChannelHeaderView, lifecycleOwner: LifecycleOwner) {
    members.observe(lifecycleOwner) { members ->
        view.setHeaderLastActive(members.lastActive(view.context))
        view.configHeaderAvatar(members)
    }
    channelState.observe(lifecycleOwner) { channel ->
        view.currentChannel = channel
        LlcMigrationUtils.getChannelNameOrMembers(channel)
            .takeUnless { it.isBlank() }
            ?.let { view.setHeaderTitle(it) }
    }
    anyOtherUsersOnline.observe(lifecycleOwner) { view.setActiveBadge(it) }
}

private fun List<Member>.lastActive(context: Context): String =
    LlcMigrationUtils.getLastActive(this).let {
        context.getString(
            R.string.stream_channel_header_active,
            when {
                it.isInLastMinute() -> context.getString(R.string.stream_channel_header_active_now)
                else -> DateUtils.getRelativeTimeSpanString(it.time).toString()
            }
        )
    }
