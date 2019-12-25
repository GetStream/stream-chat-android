package com.getstream.sdk.chat;


import com.getstream.sdk.chat.utils.StringUtility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkDownTextTest {

    @Test
    void containEmphasisTextTest() {
        String text = "*TestMarkDown*";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "_TestMarkDown_";
        assertTrue(StringUtility.containsMarkdown(text));
    }

    @Test
    void containStrongEmphasisTextTest() {
        String text = "**TestMarkDown**";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "__TestMarkDown__";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "==TestMarkDown==";
        assertTrue(StringUtility.containsMarkdown(text));
    }

    @Test
    void containStrikeThroughTextTest() {
        String text = "~~TestMarkDown~~";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "~ TestMarkDown";
        assertTrue(StringUtility.containsMarkdown(text));
    }

    @Test
    void containHeaderTextTest() {
        String text = "# h1";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "## h2";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "### h3";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "#### h4";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "##### h5";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "###### h6";
        assertTrue(StringUtility.containsMarkdown(text));
    }

    @Test
    void containLinkTextTest() {
        String text = "[GetStream](https://getstream.io/)";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "Autoconverted link https://github.com/nodeca/pica (enable linkify to see)";
        assertTrue(StringUtility.containsMarkdown(text));
    }

    @Test
    void containImageTextTest() {
        String text = "![Minion](https://octodex.github.com/images/minion.png)";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "![Stormtroopocat](https://octodex.github.com/images/stormtroopocat.jpg \"The Stormtroopocat\")";
        assertTrue(StringUtility.containsMarkdown(text));
    }

    @Test
    void containThematicBreakTextTest() {
        String text = "___  ";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "---  ";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "***  ";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "+++  ";
        assertTrue(StringUtility.containsMarkdown(text));

        text = ":::  ";
        assertTrue(StringUtility.containsMarkdown(text));
    }

    @Test
    void containCodeTextTest() {
        String text = "`code`";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "// Some comments\n" +
                "line 1 of code\n" +
                "line 2 of code\n" +
                "line 3 of code";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "```c" +
                "Sample text here...\n" +
                "```";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "``` js\n" +
                "var foo = function (bar) {\n" +
                "  return bar++;\n" +
                "};\n" +
                "\n" +
                "console.log(foo(5));\n" +
                "```";
        assertTrue(StringUtility.containsMarkdown(text));
    }

    @Test
    void notContainMarkdownTextTest() {
        String text = "TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));

        text = "*TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));

        text = "**TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));

        text = "#TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));

        text = "##TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));

        text = "`TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));
    }
}