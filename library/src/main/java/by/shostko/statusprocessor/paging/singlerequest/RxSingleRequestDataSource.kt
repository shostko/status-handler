package by.shostko.statusprocessor.paging.singlerequest

import by.shostko.statusprocessor.BaseStatusProcessor
import by.shostko.statusprocessor.paging.pagekeyed.BasePageKeyedDataSource
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@Suppress("unused")
abstract class RxSingleRequestDataSource<V>(
    statusProcessor: BaseStatusProcessor<*>,
    private val scheduler: Scheduler = Schedulers.io()
) : BasePageKeyedDataSource<Int, V>(statusProcessor) {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    override fun onLoadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, V>) {
        onLoad()
            .subscribeOn(scheduler)
            .observeOn(scheduler)
            .autoDisposable(scopeProvider)
            .subscribe({
                onSuccessResultInitial(it, null, null, params, callback)
            }, {
                onFailedResultInitial(it, params, callback)
            })
    }

    override fun onLoadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultAfter(emptyList(), null, params, callback)
    }

    override fun onLoadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, V>) {
        onSuccessResultBefore(emptyList(), null, params, callback)
    }

    @Throws(Throwable::class)
    protected abstract fun onLoad(): Single<List<V>>
}