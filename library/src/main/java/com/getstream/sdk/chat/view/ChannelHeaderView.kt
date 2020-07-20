package com.getstream.sdk.chat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamViewChannelHeaderBinding
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.view.MessageListView.HeaderAvatarGroupClickListener
import com.getstream.sdk.chat.view.MessageListView.HeaderOptionsClickListener
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member

class ChannelHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val binding: StreamViewChannelHeaderBinding
    private val style: ChannelHeaderViewStyle
    var onBackClick: () -> Unit = { }
    var currentChannel: Channel? = null

    init {
        binding = initBinding(context)
        style = ChannelHeaderViewStyle(context, attrs)
        applyStyle()
    }

    fun setHeaderTitle(title: String?) {
        binding.tvChannelName.text = title
    }

    fun setHeaderLastActive(lastActive: String?) {
        binding.tvActive.text = lastActive
    }

    fun setActiveBadge(isActive: Boolean) {
        if (isActive) {
            binding.ivActiveBadge.setImageResource(R.drawable.stream_circle_user_online)
        } else {
            binding.ivActiveBadge.setImageResource(R.drawable.stream_circle_user_offline)
        }
    }

    fun configHeaderAvatar(members: List<Member?>?) {
        binding.avatarGroup.setLastActiveUsers(LlcMigrationUtils.getOtherUsers(members), style)
    }

    private fun initBinding(context: Context): StreamViewChannelHeaderBinding =
        StreamViewChannelHeaderBinding.inflate(LayoutInflater.from(context), this, true).apply {
            btnBack.setOnClickListener { onBackClick() }
        }

    fun setHeaderOptionsClickListener(headerOptionsClickListener: HeaderOptionsClickListener) {
        binding.btnOption.setOnClickListener {
            currentChannel?.let { headerOptionsClickListener.onHeaderOptionsClick(it) }
        }
    }

    fun setHeaderAvatarGroupClickListener(headerOptionsClickListener: HeaderAvatarGroupClickListener) {
        binding.avatarGroup.setOnClickListener {
            currentChannel?.let { headerOptionsClickListener.onHeaderAvatarGroupClick(it) }
        }
    }

    private fun applyStyle() {
        style.channelTitleText.apply(binding.tvChannelName)
        style.lastActiveText.apply(binding.tvActive)
        binding.tvActive.visibility = if (style.isLastActiveShow) View.VISIBLE else View.GONE
        binding.btnBack.visibility = if (style.isBackButtonShow) View.VISIBLE else View.GONE
        binding.btnBack.background = style.backButtonBackground
        binding.avatarGroup.visibility = if (style.isAvatarGroupShow) View.VISIBLE else View.GONE
        binding.btnOption.visibility = if (style.isOptionsButtonShow) View.VISIBLE else View.GONE
        binding.btnOption.background = style.optionsButtonBackground
        binding.btnOption.textSize = style.optionsButtonTextSize.toFloat()
        binding.btnOption.width = style.optionsButtonWidth
        binding.btnOption.height = style.optionsButtonHeight
        if (!style.isAvatarGroupShow) binding.ivActiveBadge.visibility =
            View.GONE else binding.ivActiveBadge.visibility =
            if (style.isActiveBadgeShow) View.VISIBLE else View.GONE
    }
}
