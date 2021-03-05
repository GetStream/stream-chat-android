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
                    {
                        "push_provider":"firebase",
                        "id":"f8LrpCuiSB6kP7zNInnWCa:APA91bEm9zJ0hc0FtKYYMJkGMaUbP-58_YGxIksEJ8POxTxP-CCBDphSe5tFbBDrK3wsgfKRZ91j5DdFg1m7esyh1dB51U0yQ2uHD19FuwK8vMB65uayw2myYxMGzErCPz8WTweMTV94",
                        "created_at":"2021-01-21T09:13:02.774031Z",
                        "disabled":true,
                        "disabled_reason":"NotRegistered",
                        "user_id":"cebf562a-4806-4c64-a827-59d50aac42ba"
                    },
                    {
                        "push_provider":"firebase",
                        "id":"e_rLNv1HS--Q7zFmYYoIb5:APA91bFfRJ61xoMp1PKZ3jSljVHECv_EjUmguF5yDYo45B_aNcif8kP8SovrgOCJHv2F7iIYDS45MVOlqho2Wamh6ebr3hpPtgGZR8imUaSbtdPXelOQcGDK0PhAAAaa8s8COTVrjU0M",
                        "created_at":"2021-01-12T17:46:23.711316Z",
                        "user_id":"cebf562a-4806-4c64-a827-59d50aac42ba"
                    },
                    {
                        "push_provider":"firebase",
                        "id":"d_aeFmT7RKC5KfvR1VBGto:APA91bFqTK9uD_-rHgj4wHZd92PJ-MLpeW_FwfOm173xXpTfWcBrzejAkIRRCyyzqnFU80ViK2KFLtEz-YzMCqeLx8zI87nKEunmHR6mKgtidwDhbzEhXiyk_BAPOMSJ6TZgQp6GX4ko",
                        "created_at":"2021-01-11T08:23:59.43695Z",
                        "disabled":true,
                        "disabled_reason":"NotRegistered",
                        "user_id":"cebf562a-4806-4c64-a827-59d50aac42ba"
                    }
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

internal fun createSendMessageRequestJsonString() =
    """
        {
            "message":{
                "pinned":false,
                "silent":false,
                "attachments":[],
                "show_in_channel":false,
                "shadowed":false,
                "html":"",
                "id":"cebf562a-4806-4c64-a827-59d50aac42ba-80f7fe50-ec73-49f8-a389-3dd5b4304b6f",
                "text":"message test",
                "thread_participants":[],
                "mentioned_users":[],
                "cid":"messaging:e87283f0-a58d-4685-bf0b-729a7b6eb84d"
            }
        }
    """.trimIndent()

internal fun createSendMessageResponseJsonString() =
    """        
        {
            "message": {
                "id": "cebf562a-4806-4c64-a827-59d50aac42ba-80f7fe50-ec73-49f8-a389-3dd5b4304b6f",
                "text": "message test",
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
                    "name": "Zetra",
                    "unread_count": 33
                },
                "attachments": [],
                "latest_reactions": [],
                "own_reactions": [],
                "reaction_scores": {},
                "reply_count": 0,
                "cid": "messaging:e87283f0-a58d-4685-bf0b-729a7b6eb84d",
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

internal fun createDeleteReactionResponseJsonString() =
    """
        "message": {
        "id": "1f052c08-f682-4a83-896c-9f19a68bd2bb-78836b4a-a17d-4f5e-ad22-b6210a8bdda0",
        "text": "Fghh",
        "html": "\u003cp\u003eFghh\u003c/p\u003e\n",
        "type": "regular",
        "user": {
          "id": "1f052c08-f682-4a83-896c-9f19a68bd2bb",
          "role": "user",
          "created_at": "2020-12-03T11:54:56.394229Z",
          "updated_at": "2021-03-04T13:47:45.647043Z",
          "last_active": "2021-03-04T13:44:58.463492Z",
          "banned": false,
          "online": false,
          "image": "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FMarton.png?alt\u003dmedia",
          "invisible": false,
          "name": "Marton"
        },
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "reaction_counts": {},
        "reaction_scores": {},
        "reply_count": 0,
        "cid": "messaging:!members-rOvo1wmwXakoJ-aRjvyoAPFvULMoEFizIR9UYpSoyEM",
        "created_at": "2021-03-02T17:23:15.511051Z",
        "updated_at": "2021-03-04T19:21:23.263352Z",
        "shadowed": false,
        "mentioned_users": [],
        "silent": false,
        "pinned": false
      },
      "reaction": {
        "message_id": "1f052c08-f682-4a83-896c-9f19a68bd2bb-78836b4a-a17d-4f5e-ad22-b6210a8bdda0",
        "user_id": "29e46def-88f4-4b6a-a10c-584d10c4fdc9",
        "user": {
          "id": "29e46def-88f4-4b6a-a10c-584d10c4fdc9",
          "role": "user",
          "created_at": "2020-12-03T11:54:56.390689Z",
          "updated_at": "2021-03-04T19:21:02.486489Z",
          "last_active": "2021-03-04T19:21:02.486489Z",
          "banned": false,
          "online": true,
          "image": "https://firebasestorage.googleapis.com/v0/b/stream-chat-internal.appspot.com/o/users%2FLeandro.png?alt\u003dmedia",
          "invisible": false,
          "name": "Leandro"
        },
        "type": "like",
        "score": 1,
        "created_at": "2021-03-04T19:21:16.019659Z",
        "updated_at": "2021-03-04T19:21:16.019659Z"
      },
      "duration": "13.59ms"
    }
    """
