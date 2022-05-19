# Optimizations

There are some optimizations in the SDK to avoid waste of resouces and improve performance. They are possible to configure if unwanted by the user.

## Mutiple Calls to API

Prior to version 5.3.0, the SDK prevents the user from making multiple calls to the backend. If a call is already running, the SDK merges a new request into the current one and the data is propagated to both requesters of the `Call`. 

It possible to change the default behaviour and force calls to be always new requests to API and never merge two requests into one. 

To disable this option, use the option: 

```
ChatClient.Builder.disableDistinctApiCalls
```

If you want to controll new requests to API in a more granular way, you can use the extension function:

```
Call<T>.forceNewRequest(): Call<T>
```

The returned call will be forced to make a new request to API. 


