package by.shostko.statushandler.paging.singlerequest

import by.shostko.statushandler.StatusHandler
import by.shostko.statushandler.paging.pagekeyed.BasePageKeyedDataSource

@Suppress("unused")
abstract class SimpleSingleRequestDataSource<V>(statusHandler: StatusHandler<*>) :
    BasePageKeyedDataSource<Int, V>(statusHandler) {

    override fun onLoadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        onSuccessResultInitial(onLoad(), null, null, params, callback)
    }

    override fun onLoadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultAfter(emptyList(), null, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultBefore(emptyList(), null, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(): List<V>
}