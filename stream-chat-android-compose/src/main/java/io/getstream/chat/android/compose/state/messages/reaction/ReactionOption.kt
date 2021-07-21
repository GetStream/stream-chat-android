package io.getstream.chat.android.compose.state.messages.reaction

/**
 * UI representation of reactions.
 *
 * @param emoji - The Unicode representation of the reaction.
 * @param isSelected - If the option is selected or not (already reacted with it).
 * @param type - The String representation of the reaction, for the API. Can be any of:
 * ["like", "love", "haha", "wow", "sad", "angry"].
 * */
class ReactionOption(
    val emoji: String,
    val isSelected: Boolean,
    val type: String
)