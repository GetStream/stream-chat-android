package io.getstream.chat.ui.sample.feature.startup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.application.App
import io.getstream.chat.ui.sample.common.navigateSafely

class StartupFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findNavController().navigateSafely(
            if (App.instance.chatInitializer.isUserSet()) {
                R.id.action_startupFragmentFragment_to_homeFragment
            } else {
                R.id.action_startupFragmentFragment_to_userLoginFragment
            }
        )
    }
}
