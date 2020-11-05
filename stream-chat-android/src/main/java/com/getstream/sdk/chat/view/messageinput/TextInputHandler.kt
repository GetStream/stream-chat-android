package com.getstream.sdk.chat.view.messageinput

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

public class TextInputHandler(
    editText: EditText,
    public var containsTextListener: () -> Unit = {},
    public var emptyTextListener: () -> Unit = {}
) {

    init {
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (charSequence?.length == 0) {
                    emptyTextListener()
                } else {
                    containsTextListener()
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        }

        editText.addTextChangedListener(textWatcher)
    }
}
