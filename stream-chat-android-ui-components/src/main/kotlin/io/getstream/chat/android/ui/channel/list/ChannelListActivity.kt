package io.getstream.chat.android.ui.channel.list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiFragmentContainerBinding

public open class ChannelListActivity : AppCompatActivity() {
    private lateinit var binding: StreamUiFragmentContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StreamUiFragmentContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, createChannelListFragment())
                .commit()
        } else {
            // Clients can set listeners when creating `ChannelListFragment`, but
            // in that case they need to set the listeners once again here.
        }
    }

    protected open fun createChannelListFragment(): ChannelListFragment {
        return ChannelListFragment.newInstance {
            showSearch(true)
            showHeader(true)
        }
    }
}
