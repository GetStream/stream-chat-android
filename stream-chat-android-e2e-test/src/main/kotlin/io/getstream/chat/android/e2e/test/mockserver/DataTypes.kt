package io.getstream.chat.android.e2e.test.mockserver

public enum class AttachmentType(public val attachment: String) {
    IMAGE("image"),
    VIDEO("video"),
    FILE("file")
}

public enum class ReactionType(public val reaction: String) {
    LOVE("love"),
    LOL("haha"),
    WOW("wow"),
    SAD("sad"),
    LIKE("like")
}
