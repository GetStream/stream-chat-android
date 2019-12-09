**Setup**
To handle Firebase Push Notifications, a developer needs to register `StreamFirebaseMessagingService` in AndroidManifest:

```
<service
   android:name="com.getstream.sdk.chat.notifications.StreamFirebaseMessagingService"
   android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
</service>
```

In this case, the developer will have the default behavior.

Push notifications don't appear when the application in the foreground;
If the application goes to the background, Web Socket will keep the connection within 10 sec.
(Can be changed with WEB_SOCKET_DISCONNECT_DELAY constant in StreamChat)
It can be changed with `client.setWebSocketDisconnectDelay` method.

During this time push notification will show by WebSocket events.
Message's author's name is the title of the notification. The message text is content of the notification. On click on notification will be opened the default launcher activity of the application.