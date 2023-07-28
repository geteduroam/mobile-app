package app.eduroam.geteduroam.di.api.response

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

/**
 * [CallAdapter] which adapts Retrofit responses into [ApiResponse]
 */
class ApiResponseAdapter<S : Any>(
    private val successType: Type,
    private val errorBodyConverter: Converter<ResponseBody, S>,
) : CallAdapter<S, Call<ApiResponse<S>>> {
    override fun responseType(): Type = successType

    override fun adapt(call: Call<S>): Call<ApiResponse<S>> =
        ApiResponseCall(call, errorBodyConverter, successType)
}