package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.Event

class WSResponseHandlerImpl : WSResponseHandler {
    override fun onWSEvent(event: Event) {

        val name = Thread.currentThread().name

        if (name.isNotEmpty()) {

        }

//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connectionResolved(event: Event) {

        val name = Thread.currentThread().name

        if (name.isNotEmpty()) {

        }

//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connectionRecovered() {

        val name = Thread.currentThread().name

        if (name.isNotEmpty()) {

        }

//        clientID = event.connectionId
//        if (event.me != null && !event.isAnonymous) state.currentUser = event.me
//
//        // mark as connect, any new callbacks will automatically be executed
//        connected = true
//
//        // call onSuccess for everyone that was waiting
//        val subs = connectSubRegistry.getSubscribers()
//        connectSubRegistry.clear()
//        for (waiter in subs) {
//            waiter.onSuccess(getUser())
//        }
    }

    override fun tokenExpired() {

        val name = Thread.currentThread().name

        if (name.isNotEmpty()) {

        }

//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(error: WsErrorMessage) {

        val name = Thread.currentThread().name

        if (name.isNotEmpty()) {

        }


//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}