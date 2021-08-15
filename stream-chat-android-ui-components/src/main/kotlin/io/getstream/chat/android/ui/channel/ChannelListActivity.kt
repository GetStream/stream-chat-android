package io.getstream.chat.android.ui.channel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiFragmentContainerBinding

/**
 * An Activity representing a self-contained channel list screen. This Activity
 * is simply a thin wrapper around [ChannelListFragment].
 */
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
        }
    }

    protected open fun createChannelListFragment(): ChannelListFragment {
        return ChannelListFragment.newInstance {
            showSearch(true)
            showHeader(true)
        }
    }

    public companion object {
        public fun createIntent(context: Context): Intent {
            return Intent(context, ChannelListActivity::class.java)
        }
    }
}
