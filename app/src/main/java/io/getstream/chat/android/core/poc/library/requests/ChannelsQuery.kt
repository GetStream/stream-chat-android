package io.getstream.chat.android.core.poc.library.requests

class ChannelsQuery {
    var sort = mutableMapOf<String, Any>()
    var filter_conditions = mutableMapOf<String, Any>()
    var limit = 0
    var offset = 0
    var message_limit = 0
}