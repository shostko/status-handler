package by.shostko.statusprocessor.paging.positional

import by.shostko.statusprocessor.BaseStatusProcessor

@Suppress("unused")
abstract class SimplePositionalDataSource<V>(
    statusProcessor: BaseStatusProcessor<*>
) : BasePositionalDataSource<V>(statusProcessor) {

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