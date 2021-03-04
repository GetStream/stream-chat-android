package io.getstream.chat.android.livedata.mockwebserver

internal fun createConnectedEventStringJson() =
    """
        {
            "type":"health.check",
            "connection_id":"603cbcf1-0a05-2cee-0000-0000000eb1e3",
            "cid":"*",
            "me":{
                "id":"cebf562a-4806-4c64-a827-59d50aac42ba",
                "role":"user",
                "roles":[],
                "created_at":"2020-12-03T11:54:56.380252Z",
                "updated_at":"2021-03-03T02:53:16.517485Z",
                "last_active":"2021-03-03T02:53:16.517485Z",
                "banned":false,
                "online":true,
                "invisible":false,
                "devices":[
                    {
                        "push_provider":"firebase",
                        "id":"e5Ke9s3uRuODcw5YHXPxmq:APA91bHiyq6RmXNzTKl6wO9Uu9pOgT1sG8w5bV_whsXlndBrQH6D9d5YYJ7PhyAMHJFVRxXkMWXc9rwPgVgxsXNbEOHQXpyRbeSYqbvdl0sysaIJXwu9pqt414d2GEy8MKMp4SW2F2Sp",
                        "created_at":"2021-02-01T18:03:46.337861Z",
                        "disabled":true,
                        "disabled_reason":"NotRegistered",
                        "user_id":"cebf562a-4806-4c64-a827-59d50aac42ba"
                    },
                ],
                "mutes":[],
                "channel_mutes":[],
                "unread_count":133,
                "total_unread_count":133,
                "unread_channels":8,
                "language":"",
                "name":"Zetra",
                "image":"https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FZetra.png?alt=media"
           },
           "created_at":"2021-03-03T02:53:16.521869803Z"
       }        
    """.trimIndent()

internal fun createSendMessageRequestJsonString(
    messageId: String,
    messageText: String,
    channelId: String,
): String {
    return """
        {
            "message":{
                "pinned":false,
                "silent":false,
                "attachments":[],
                "show_in_channel":false,
                "shadowed":false,
                "html":"",
                "id":"$messageId",
                "text":"$messageText",
                "thread_participants":[],
                "mentioned_users":[],
                "cid":"$channelId"
            }
        }
    """.trimIndent()
}

internal fun createSendMessageResponseJsonString(
    messageId: String,
    messageText: String,
    channelId: String,
    username: String,
): String {
    return """        
        {
            "message": {
                "id": "$messageId",
                "text": "$messageText",
                "html": "\u003cp\u003emessage test\u003c/p\u003e\n",
                "type": "regular",
                "user": {
                    "id": "cebf562a-4806-4c64-a827-59d50aac42ba",
                    "role": "user",
                    "created_at": "2020-12-03T11:54:56.380252Z",
                    "updated_at": "2021-03-03T02:53:16.517485Z",
                    "last_active": "2021-03-03T02:53:16.517485Z",
                    "banned": false,
                    "online": true,
                    "image": "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FZetra.png?alt\u003dmedia",
                    "invisible": false,
                    "name": "$username",
                    "unread_count": 33
                },
                "attachments": [],
                "latest_reactions": [],
                "own_reactions": [],
                "reaction_scores": {},
                "reply_count": 0,
                "cid": "$channelId",
                "created_at": "2021-03-03T02:57:17.64866Z",
                "updated_at": "2021-03-03T02:57:17.648661Z",
                "shadowed": false,
                "mentioned_users": [],
                "silent": false,
                "pinned": false
            },
            "duration": "50.92ms"
        }
    """.trimIndent()
}
