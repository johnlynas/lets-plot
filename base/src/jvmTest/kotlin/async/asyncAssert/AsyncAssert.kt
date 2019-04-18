package jetbrains.datalore.base.async.asyncAssert

import jetbrains.datalore.base.async.Async
import jetbrains.datalore.base.async.asyncAssert.AsyncResult.AsyncState
import jetbrains.datalore.base.async.asyncAssert.AsyncResult.Companion.getResult
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions


class AsyncAssert<ItemT> private constructor(actual: Async<ItemT>) :
        AbstractAssert<AsyncAssert<ItemT>, Async<ItemT>>(actual, AsyncAssert::class.java) {

    companion object {

        internal val FORMAT_SUCCEEDED_ASYNC = "async is succeeded with %s"
        internal val FORMAT_FAILED_ASYNC = "async is failed with %s"
        internal val UNFINISHED_ASYNC = "async isn't finished yet"

        fun <ItemT> assertThat(actual: Async<ItemT>): AsyncAssert<ItemT> {
            return AsyncAssert(actual)
        }
    }

    fun result(): AbstractObjectAssert<*, ItemT> {
        val result = getResult(actual)
        failIfUnfinished(result)
        failIfFailed(result)
        return Assertions.assertThat(result.value)
    }

    fun succeeded(): AsyncAssert<ItemT> {
        result()
        return this
    }

    fun succeededWith(value: ItemT?): AsyncAssert<ItemT> {
        result().isEqualTo(value)
        return this
    }

    fun failed(): AsyncAssert<ItemT> {
        failure()
        return this
    }

    fun failure(): AbstractThrowableAssert<*, out Throwable> {
        val result = getResult(actual)
        failIfUnfinished(result)
        failIfSucceeded(result)
        return Assertions.assertThat(result.error)
    }

    fun <ErrorT> failureIs(failureClass: Class<ErrorT>): AsyncAssert<ItemT> {
        failure().isInstanceOf(failureClass)
        return this
    }

    fun <ErrorT> failureIs(failureClass: Class<ErrorT>, message: String): AsyncAssert<ItemT> {
        failure().hasMessage(message)
        return failureIs(failureClass)
    }

    fun unfinished(): AsyncAssert<ItemT> {
        val result = getResult(actual)
        failIfSucceeded(result)
        failIfFailed(result)
        return this
    }

    fun willBeFinishedAfter(r: Runnable): AsyncAssert<ItemT> {
        unfinished()
        r.run()
        val result = getResult(actual)
        failIfUnfinished(result)
        return this
    }

    private fun failIfSucceeded(result: AsyncResult<ItemT>) {
        if (result.state === AsyncState.SUCCEEDED) {
            failWithMessage(FORMAT_SUCCEEDED_ASYNC, result.value)
        }
    }

    private fun failIfFailed(result: AsyncResult<ItemT>) {
        if (result.state === AsyncState.FAILED) {
            failWithMessage(FORMAT_FAILED_ASYNC, result.error)
        }
    }

    private fun failIfUnfinished(result: AsyncResult<ItemT>) {
        if (result.state === AsyncState.UNFINISHED) {
            failWithMessage(UNFINISHED_ASYNC)
        }
    }
}