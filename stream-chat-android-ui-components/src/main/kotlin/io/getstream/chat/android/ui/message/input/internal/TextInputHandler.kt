package io.getstream.chat.android.ui.message.input.internal

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

internal object TextInputHandler {

    var containsText = false

    fun bindEditText(editText: EditText, containsTextListener: () -> Unit = {}, emptyTextListener: () -> Unit = {}) {
        object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (charSequence?.length == 0 && containsText) {
                    emptyTextListener()
                    containsText = false
                } else if (!containsText) {
                    containsText = true
                    containsTextListener()
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        }.let(editText::addTextChangedListener)
    }
}
