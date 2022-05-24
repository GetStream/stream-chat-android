# Optimizations

The SDK is optimized to avoid waste of resources and improve performance. You can freely choose which optimizations should be enabled or disabled.

## Multiple Calls to API

After version `5.3.0`, the SDK prevents the user from making multiple calls to the backend. If a call is already running, the SDK merges a new request into the current one and the data is propagated to both requesters of the `Call`. 

You can change the default behavior and force calls to always make new requests to API and never merge two requests into one by using:


```
ChatClient.Builder.disableDistinctApiCalls()
```

If you want to control new requests to API in a more granular way, you can use the extension function:

```
Call<T>.forceNewRequest(): Call<T>
```

The returned `Call` will be forced to make a new request to API. 


