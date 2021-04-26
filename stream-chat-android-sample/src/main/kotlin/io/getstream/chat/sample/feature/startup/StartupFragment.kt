package io.getstream.chat.sample.feature.startup

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.sample.R
import io.getstream.chat.sample.application.App
import io.getstream.chat.sample.common.navigateSafely

class StartupFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showDeprecationWarningIfNeeded()

        findNavController().navigateSafely(
            if (App.instance.chatInitializer.isUserSet()) {
                R.id.action_startupFragmentFragment_to_channelsFragment
            } else {
                R.id.action_startupFragmentFragment_to_userLoginFragment
            }
        )
    }

    private fun showDeprecationWarningIfNeeded() {
        val context = requireContext()

        if (warningShown(context)) {
            return
        }

        val checkbox = CheckBox(context).apply { text = "Do not show this again" }
        val frame = FrameLayout(context).apply {
            setPadding(Utils.dpToPx(16), Utils.dpToPx(12), Utils.dpToPx(16), 0)
        }
        frame.addView(checkbox)

        AlertDialog.Builder(context)
            .setTitle("Deprecation warning")
            .setMessage(Html.fromHtml("This sample uses our old UI kit.<br/><br/>Take a look at the new UI Components implementation in the <b>ui-components</b> and <b>ui-components-sample</b> modules instead!"))
            .setView(frame)
            .setPositiveButton("Ok") { _, _ ->
                if (checkbox.isChecked) {
                    setWarningShown(context)
                }
            }
            .show()
    }

    private fun warningShown(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(OLD_SAMPLE_WARNING_SHOWN_KEY, false)
    }

    private fun setWarningShown(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(OLD_SAMPLE_WARNING_SHOWN_KEY, true)
        }
    }

    private companion object {
        private const val OLD_SAMPLE_WARNING_SHOWN_KEY = "old_sample_warning_shown"
    }
}
