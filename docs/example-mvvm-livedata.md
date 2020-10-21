# MVVM + LiveData
## Repository

```kotlin
class ChannelsRepository (
    private val client: Client,
    private val cache: ChannelsCache
) {

    fun getChannels(): LiveData<List<Channel>> {
        val call = client.queryChannels()
        val live = cache.getAllLive()

        call.enqueue { result ->
            if (result.isSuccess()) {
                cache.storeAsync(result.data())
            }
        }

        return live
    }
}
```

## ViewModel

```kotlin
class ChannelsViewModel(private val repository: ChannelsRepository) {

    fun channels(): LiveData<ViewState<List<Channel>>> {

        val liveData = MediatorLiveData<ViewState<List<Channel>>>()

        liveData.addSource(MutableLiveData<ViewState<List<Channel>>>(ViewState.Loading())) {
            liveData.value = it
        }

        liveData.addSource(Transformations.map(repository.getChannels()) { channels ->
            ViewState.Success(channels)
        }) {
            if (it.data.isNotEmpty()) liveData.value = it
        }

        return liveData
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
        val vm = ChannelsViewModel(App.channelsRepositoryLive)

        vm.channels().observe(this,
            Observer<ViewState<List<Channel>>> { state ->
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
