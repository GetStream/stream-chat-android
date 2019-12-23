# MVVM + RxJava
## Repository

```kotlin
class ChannelsRepository (
    private val client: Client,
    private val cache: ChannelsCache
) {

    fun getChannels(): Observable<List<Channel>> {
        return merge(getCached(), updateCache())
    }

    private fun updateCache(): Observable<List<Channel>> {
        return fromAction {
            val result = client.queryChannels().execute()
            if (result.isSuccess())
                cache.storeSync(ApiMapper.mapChannels(result.data()))
            else 
                throw RuntimeException("Channels loading error", result.error())
        }.toObservable()
    }

    private fun getCached(): Observable<List<Channel>> {
        return cache.getAllChannels().distinct().flatMap {
            just(it)
        }
    }
}
```

## ViewModel

```kotlin
class ChannelsViewModel(val repository: ChannelsRepository) {

    fun channels(): Observable<ViewState<List<Channel>>> {
        return loadChannels()
    }

    private fun loadChannels(): Observable<ViewState<List<Channel>>> {
        return repository.getChannels()
            .map<ViewState<List<Channel>>> { Success(it) }
            .startWith(ViewState.Loading())
            .onErrorReturn { ViewState.Error(it) }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}
```

## View

```kotlin
class ChannelsFragment: Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        load()
    }

    private fun load() {
        val vm = ChannelsViewModel(ChannelsRepository())

        vm.channels().subscribe {
            when (it) {
                is ViewState.Loading -> {
                    drawLoading()
                }
                is ViewState.Error -> {
                    drawError(it.error)
                }
                is ViewState.Success -> {
                    drawSuccess(it.data)
                }
            }
        }
    }
}
```