package io.getstream.chat.android.ui.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import io.getstream.chat.android.ui.databinding.StreamSearchViewBinding

public class SearchView : FrameLayout {

    public var listener: Listener? = null

    private val binding: StreamSearchViewBinding =
        StreamSearchViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val query: String
        get() = binding.inputField.text.trim().toString()

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)

        binding.inputField.doAfterTextChanged { newText ->
            updateClearButtonVisibility(newText)
            listener?.onInputChanged(query)
        }
        binding.inputField.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    listener?.onSearchStarted(query)
                    true
                }
                else -> false
            }
        }

        binding.clearInputButton.setOnClickListener {
            binding.inputField.setText("")
        }

        updateClearButtonVisibility(query)
    }

    private fun updateClearButtonVisibility(text: CharSequence?) {
        binding.clearInputButton.isVisible = !text.isNullOrEmpty()
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    /**
     * Updates the current input to the specified string.
     *
     * Note that listeners are notified of this change.
     */
    public fun setQuery(query: String) {
        binding.inputField.setText(query.trim())
    }

    public interface Listener {
        /**
         * Notifies observers of every individual change in the input, per character.
         */
        public fun onInputChanged(query: String)

        /**
         * Search was started with keyboard search button.
         */
        public fun onSearchStarted(query: String)
    }
}
