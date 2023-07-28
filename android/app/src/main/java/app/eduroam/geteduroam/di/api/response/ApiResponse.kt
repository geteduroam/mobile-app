package app.eduroam.geteduroam.di.api.response

import okhttp3.Headers
import retrofit2.Retrofit
import java.io.IOException

/**
 * Wrapper for [Retrofit] responses to parse an error body with the success body type.
 */
sealed class ApiResponse<out S : Any> {
    /**
     * Success response (2xx status code) with body
     */
    data class Success<S : Any>(
        val body: S,
        val code: Int,
        val headers: Headers,
    ) : ApiResponse<S>()

    /**
     * Failure response (non-2xx status code) with a optional body
     */
    data class Failure<S : Any>(
        val body: S?,
        val code: Int,
        val headers: Headers,
    ) : ApiResponse<S>()

    /**
     * Network error
     */
    data class NetworkError(
        val error: IOException,
    ) : ApiResponse<Nothing>()

    /**
     * Any other non-network error
     */
    data class Error(
        val error: Throwable,
        val code: Int? = null,
    ) : ApiResponse<Nothing>()
}
