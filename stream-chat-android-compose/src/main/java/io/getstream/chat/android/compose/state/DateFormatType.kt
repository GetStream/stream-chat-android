package io.getstream.chat.android.compose.state

/**
 * Represents the types of formatting we provide with our formatter API.
 *
 * This is used to control if we want to show timestamps, or full date information.
 */
public enum class DateFormatType {
    /**
     * Represents a date format with only timestamps.
     */
    TIME,

    /**
     * Represents a date format with full date information.
     */
    DATE,
}
