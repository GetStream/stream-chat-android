# MVVM + Coroutines
## Repository

```kotlin
class ChannelsRepository(
    private val client: Client,
    private val cache: ChannelsCache
) {
    fun getChannels(): List<Channel> {
        val result = client.queryChannels().execute()
        return if (result.isSuccess()) {
            val channels = ApiMapper.mapChannels(result.data())
            cache.storeSync(channels)
            channels
        } else {
            cache.getAllSync()
        }
    }
}
```

## ViewModel

```kotlin
class ChannelsViewModel(repository: ChannelsRepository) {

    val channels = MutableLiveData<ViewState<List<Channel>>>()

    val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    val bgDispatcher: CoroutineDispatcher = Dispatchers.IO

    val uiScope = CoroutineScope(uiDispatcher)
    val bgScope = CoroutineScope(bgDispatcher)

    init {

        channels.postValue(ViewState.Loading())

        uiScope.launch {
            val result = withContext(bgDispatcher) { repository.getChannels() }
            channels.postValue(ViewState.Success(result))
        }
    }

    fun channels(): LiveData<ViewState<List<Channel>>> {
        return channels
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

        vm.channels().observe(this, Observer<ViewState<List<Channel>>> { state ->
            when (state) {
                is ViewState.Loading -> {
                    drawLoading()
                }
                is ViewState.Error -> {
                    drawError(state.error)
                }
                is ViewState.Success -> {
                    drawSuccess(state.data)
                }
            }
        })
    }
}
```