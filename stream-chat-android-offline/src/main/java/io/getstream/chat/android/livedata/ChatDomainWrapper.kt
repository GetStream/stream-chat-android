package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

public class ChatDomainWrapper(private var client: ChatClient) {
    private var domain: ChatDomain? = null

    public fun setUser(user: User) {
        domain = factory.create(appContext, client, user, database, storageEnabled, userPresence, recoveryEnabled)
    }

    init {
        client.preSetUserListeners.add {
            // start the db
            setUser(it)
        }
        client.disconnectListeners.add {
            GlobalScope.launch {
                ChatDomain.instance?.let {
                    it.disconnect()
                    ChatDomain.instance = null
                }
            }
        }

    }

}