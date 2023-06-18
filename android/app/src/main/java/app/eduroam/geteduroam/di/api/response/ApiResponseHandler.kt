package app.eduroam.geteduroam.di.api.response

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.lang.reflect.Type

internal object ApiResponseHandler {
    /**
     * Converts the given [response] to a subclass of [ApiResponse] based on different conditions.
     *
     * If the server response is successful with:
     * => a non-empty body -> NetworkResponse.Success<S>
     * => an empty body (and [successType] is not Unit) -> NetworkResponse.ServerError<S>
     * => an empty body (and [successType] is Unit) -> NetworkResponse.Success<Unit>
     *
     * If the servers response is not successful:
     * => a non-empty body -> NetworkResponse.Failure<S>
     * => an empty body -> NetworkResponse.Failure<S>
     * => errors -> NetworkResponse.Error
     */
    fun <S : Any> handle(
        response: Response<S>,
        successType: Type,
        errorConverter: Converter<ResponseBody, S>,
    ): ApiResponse<S> {
        val body = response.body()
        val headers = response.headers()
        val code = response.code()
        val errorBody = response.errorBody()

        return if (response.isSuccessful) {
            if (body != null) {
                ApiResponse.Success(body = body, code = code, headers = headers)
            } else {
                if (successType == Unit::class.java) {
                    @Suppress("UNCHECKED_CAST")
                    ApiResponse.Success(
                        body = Unit,
                        code = code,
                        headers = headers
                    ) as ApiResponse<S>
                } else {
                    ApiResponse.Failure(body = null, code = code, headers = headers)
                }
            }
        } else {
            return try {
                val convertedBody = if (errorBody == null) {
                    null
                } else {
                    errorConverter.convert(errorBody)
                }
                if (successType == Unit::class.java) {
                    @Suppress("UNCHECKED_CAST")
                    ApiResponse.Success(
                        body = Unit,
                        code = code,
                        headers = headers
                    ) as ApiResponse<S>
                } else {
                    ApiResponse.Failure(body = convertedBody, code = code, headers = headers)
                }
            } catch (ex: Exception) {
                ApiResponse.Error(error = ex, code = code)
            }
        }
    }
}
