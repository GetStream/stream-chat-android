package io.getstream.chat.android.ui.message

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiFragmentContainerBinding

/**
 * An Activity representing a self-contained chat screen. This Activity is simply
 * a thin wrapper around [MessageListFragment].
 */
public open class MessageListActivity : AppCompatActivity() {
    private lateinit var binding: StreamUiFragmentContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StreamUiFragmentContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val cid = requireNotNull(intent.getStringExtra(EXTRA_CID)) {
                "Channel cid must not be null"
            }
            val messageId = intent.getStringExtra(EXTRA_MESSAGE_ID)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, createMessageListFragment(cid, messageId))
                .commit()
        }
    }

    /**
     * Creates an instance of [MessageListFragment]. Override this method if you want to create an
     * instance of [MessageListFragment] with custom arguments or if you want to create a subclass
     * of [MessageListFragment].
     */
    protected open fun createMessageListFragment(cid: String, messageId: String?): MessageListFragment {
        return MessageListFragment.newInstance(cid) {
            setFragment(MessageListFragment())
            showHeader(true)
            messageId(messageId)
        }
    }

    public companion object {
        private const val EXTRA_CID: String = "extra_cid"
        private const val EXTRA_MESSAGE_ID: String = "extra_message_id"

        public fun createIntent(context: Context, cid: String, messageId: String? = null): Intent {
            return Intent(context, MessageListActivity::class.java).apply {
                putExtra(EXTRA_CID, cid)
                putExtra(EXTRA_MESSAGE_ID, messageId)
            }
        }
    }
}
