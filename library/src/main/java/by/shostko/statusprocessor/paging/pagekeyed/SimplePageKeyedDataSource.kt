package by.shostko.statusprocessor.paging.pagekeyed

import by.shostko.statusprocessor.BaseStatusProcessor

@Suppress("MemberVisibilityCanBePrivate", "unused", "CheckResult")
abstract class SimplePageKeyedDataSource<K, V>(
    statusProcessor: BaseStatusProcessor<*>,
    protected val firstPageKey: K
) : BasePageKeyedDataSource<K, V>(statusProcessor) {

    override fun onLoadInitial(params: LoadInitialParams<K>, callback: LoadInitialCallback<K, V>) {
        val list = onLoad(firstPageKey, params.requestedLoadSize)
        val previousPageKey = prevKey(firstPageKey)
        val nextPageKey = nextKey(firstPageKey)
        onSuccessResultInitial(list, previousPageKey, nextPageKey, params, callback)
    }

    override fun onLoadAfter(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val list = onLoad(params.key, params.requestedLoadSize)
        val nextPageKey = nextKey(firstPageKey)
        onSuccessResultAfter(list, nextPageKey, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<K>, callback: LoadCallback<K, V>) {
        val list = onLoad(params.key, params.requestedLoadSize)
        val previousPageKey = prevKey(firstPageKey)
        onSuccessResultBefore(list, previousPageKey, params, callback)
    }

    protected abstract fun nextKey(key: K): K?

    protected abstract fun prevKey(key: K): K?

    @Throws(Throwable::class)
    protected abstract fun onLoad(key: K, requestedLoadSize: Int): List<V>
}