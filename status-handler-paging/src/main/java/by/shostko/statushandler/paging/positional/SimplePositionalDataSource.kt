package by.shostko.statushandler.paging.positional

import by.shostko.statushandler.StatusHandler

@Suppress("unused")
abstract class SimplePositionalDataSource<V>(
    statusHandler: StatusHandler<*>
) : BasePositionalDataSource<V>(statusHandler) {

    override fun onLoadInitial(params: LoadInitialParams, callback: LoadInitialCallback<V>) {
        val list = onLoad(params.requestedStartPosition, params.requestedLoadSize)
        onSuccessResult(list, params, callback)
    }

    override fun onLoadRange(params: LoadRangeParams, callback: LoadRangeCallback<V>) {
        val list = onLoad(params.startPosition, params.loadSize)
        onSuccessResult(list, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(startPosition: Int, loadSize: Int): List<V>
}