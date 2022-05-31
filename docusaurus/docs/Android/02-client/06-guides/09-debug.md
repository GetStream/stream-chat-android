# Debug

The SDK include a tool to debug requests and help the user to understand how and when the backend is being requested

## ApiRequestsAnalyser

The `ApiRequestsAnalyser` can be called at any time to print all the requests that were made with its informations. 

Enable it using: 

```
ChatClient.Builder(apiKey, context)
    .debugRequests(true)
    .build()
```

Then you can request the information for the requests used `ApiRequestsAnalyser.dumpRequestByName` or `ApiRequestsAnalyser.dumpAll`. 

To clear the information of the analyser to focus on some infomation, it is possible to clear the data using `ApiRequestsAnalyser.clearRequestContaining` or `ApiRequestsAnalyse.clearAll`.