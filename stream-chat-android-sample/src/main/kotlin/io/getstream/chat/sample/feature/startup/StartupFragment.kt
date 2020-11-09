package io.getstream.chat.sample.feature.startup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.navigateSafely

class StartupFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findNavController().navigateSafely(
            if (ChatClient.isInitialized && ChatClient.instance().getCurrentUser() != null) {
                R.id.action_startupFragmentFragment_to_channelsFragment
            } else {
                R.id.action_startupFragmentFragment_to_userLoginFragment
            }
        )
    }
}
