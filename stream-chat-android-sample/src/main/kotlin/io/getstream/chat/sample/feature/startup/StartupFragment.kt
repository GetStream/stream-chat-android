package io.getstream.chat.sample.feature.startup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.getstream.chat.sample.R
import io.getstream.chat.sample.application.ChatInitializer
import io.getstream.chat.sample.common.navigateSafely
import org.koin.android.ext.android.inject

class StartupFragment : Fragment() {

    private val chatInitializer: ChatInitializer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findNavController().navigateSafely(
            if (chatInitializer.isUserSet()) {
                R.id.action_startupFragmentFragment_to_channelsFragment
            } else {
                R.id.action_startupFragmentFragment_to_userLoginFragment
            }
        )
    }
}
