package com.getstream.sdk.chat.utils.strings;

import com.getstream.sdk.chat.R;

public class StubChatStrings implements ChatStrings {

    @Override
    public String get(int resId) {
        if (resId == R.string.stream_today) {
            return "today";
        } else if (resId == R.string.stream_yesterday) {
            return "yesterday";
        } else if (resId == R.string.stream_just_now) {
            return "now";
        } else {
            return null;
        }
    }
}
