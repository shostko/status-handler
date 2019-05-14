package by.shostko.statusprocessor

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.*

enum class Action {
    REFRESH,
    RETRY
}

class StatusProcessor(lifecycleOwner: LifecycleOwner) : BaseStatusProcessor<LoadingStatus>(lifecycleOwner) {

    override fun createStatusLoading(): LoadingStatus = LoadingStatus.loading()

    override fun createStatusError(throwable: Throwable): LoadingStatus = LoadingStatus.error(throwable)

    override fun createStatusError(workerResult: Data): LoadingStatus = LoadingStatus.fromWorkerFailedData(workerResult)

    override fun createStatusSuccess(): LoadingStatus = LoadingStatus.success()

    override fun createStatusSuccess(workerResult: Data): LoadingStatus = LoadingStatus.success()
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class BaseStatusProcessor<STATUS>(private val lifecycleOwner: LifecycleOwner) {

    @Suppress("LeakingThis")
    private val statusProcessor: FlowableProcessor<STATUS> = BehaviorProcessor.createDefault(createStatusLoading())

    private val actionProcessor: FlowableProcessor<Action> = PublishProcessor.create()

    val status: Flowable<STATUS> = statusProcessor.hide()

    val action: Flowable<Action> = actionProcessor.hide()

    fun retry() {
        actionProcessor.onNext(Action.RETRY)
    }

    fun refresh() {
        actionProcessor.onNext(Action.REFRESH)
    }

    fun update(loadingStatus: STATUS) {
        statusProcessor.onNext(loadingStatus)
    }

    protected abstract fun createStatusLoading(): STATUS

    protected abstract fun createStatusError(throwable: Throwable): STATUS

    protected abstract fun createStatusError(workerResult: Data): STATUS

    protected abstract fun createStatusSuccess(): STATUS

    protected abstract fun createStatusSuccess(workerResult: Data): STATUS

    fun <T> combineWithWorker(callable: () -> UUID): FlowableTransformer<T, T> = FlowableTransformer { upstream ->
        Flowable.combineLatest(
            action.startWith(Action.REFRESH)
                .doOnNext {
                    val uuid = callable.invoke()
                    Handler(Looper.getMainLooper()).post {
                        WorkManager.getInstance().getWorkInfoByIdLiveData(uuid)
                            .observe(lifecycleOwner, Observer { info ->
                                info?.state?.let {
                                    when {
                                        !it.isFinished -> update(createStatusLoading())
                                        it == WorkInfo.State.FAILED -> update(createStatusError(info.outputData))
                                        else -> update(createStatusSuccess(info.outputData))
                                    }
                                }
                            })
                    }
                }
                .ignoreElements()
                .toFlowable<Unit>()
                .startWith(Unit)
                .distinctUntilChanged(),
            upstream, BiFunction { _, t -> t })
    }

    fun <T> wrapSingleRequest(callable: () -> Single<List<T>>): Flowable<List<T>> = wrapSingleRequest(emptyList(), callable)

    fun <T> wrapSingleRequest(errorItem: T, callable: () -> Single<T>): Flowable<T> =
        action.startWith(Action.REFRESH)
            .doOnNext { update(createStatusLoading()) }
            .switchMapSingle {
                callable.invoke()
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { update(createStatusSuccess()) }
                    .doOnError { update(createStatusError(it)) }
                    .onErrorReturnItem(errorItem)
            }
}