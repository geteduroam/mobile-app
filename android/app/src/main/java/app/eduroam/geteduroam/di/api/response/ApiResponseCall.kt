package app.eduroam.geteduroam.di.api.response

import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

/**
 * [Call] to handle [enqueue] [ApiResponse]
 */
internal class ApiResponseCall<S : Any>(
    private val delegate: Call<S>,
    private val errorConverter: Converter<ResponseBody, S>,
    private val successType: Type,
) : Call<ApiResponse<S>> {
    override fun enqueue(callback: Callback<ApiResponse<S>>) {
        return delegate.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val networkResponse =
                    ApiResponseHandler.handle(response, successType, errorConverter)
                callback.onResponse(this@ApiResponseCall, Response.success(networkResponse))
            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {
                val networkResponse = when (throwable) {
                    is IOException -> ApiResponse.NetworkError(throwable)
                    else -> ApiResponse.Error(throwable)
                }
                callback.onResponse(this@ApiResponseCall, Response.success(networkResponse))
            }
        })
    }

    override fun isExecuted() = synchronized(this) { delegate.isExecuted }

    override fun clone() = ApiResponseCall(delegate.clone(), errorConverter, successType)

    override fun isCanceled() = synchronized(this) { delegate.isCanceled }

    override fun cancel() = synchronized(this) { delegate.cancel() }

    override fun execute(): Response<ApiResponse<S>> {
        throw UnsupportedOperationException("ApiResponseCall doesn't support synchronous execution")
    }

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()
}