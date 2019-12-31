package com.getstream.sdk.chat.utils;

import java.util.regex.Pattern;

public class RegexUtils {

    private static Pattern ALL_MARKDOWN_PATTERN;

    private static final String MARKDOWN_REGEX = "<([a-zA-Z0-9. ]+)>|\\*{1,3}([a-zA-Z0-9. ]+)\\*{1,3}|_(\\w+)_|=(\\w+)=" +
            "|~(\\w+)~|~(\\w+)|~ (\\w+)|@(\\w+)|#{1,6} (\\w+)|---( +)|\\*\\*\\*( +)|\\+\\+\\+( +)" +
            "|:::( +)|// ?|`{1,3} ?\\n? ?([a-zA-Z0-9. ]+)\\n?`{1,3}|`{1,3}(\\w+)\\n([a-zA-Z0-9. ]+)\\n?`{1,3}" +
            "|`{1,3} ?\\w+?\\n? ?([a-zA-Z0-9. ]+)\\n?`{1,3}|`{3}([a-zA-Z0-9. ]+)";

    public static Pattern getAllMarkdownPattern() {
        if (ALL_MARKDOWN_PATTERN == null)
            ALL_MARKDOWN_PATTERN = Pattern.compile(MARKDOWN_REGEX, Pattern.DOTALL);
        return ALL_MARKDOWN_PATTERN;
    }
}
