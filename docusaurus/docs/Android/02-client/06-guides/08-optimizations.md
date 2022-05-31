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


## QuerySorter
To sort your object and present then in a desired order, tt is possible to choose between 2 implementations of QuerySorter: `QuerySort` and `QuerySortByMap`. 

`QuerySort` uses reflection to find the fields that should be used to compare when sorting. This avoids the need for extra implementation to make a class able to be sorted. 

The drawback is that reflection is an expensive operation and this implementation will have a lower performance compared to `QuerySortByMap`.

On the other hand, `QuerySortByMap` needs the class to be sorted to extend from `QueryableByMap` and provide the fields to be used in the comparation in a map. This implementation doesn't use reflection, so it will run faster. 
