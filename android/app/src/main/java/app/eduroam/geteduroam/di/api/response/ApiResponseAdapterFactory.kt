package app.eduroam.geteduroam.di.api.response

import app.eduroam.geteduroam.di.api.response.ApiResponseAdapterFactory.Companion.create
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Factory to [create] an [ApiResponseAdapter] instance
 */
class ApiResponseAdapterFactory private constructor() : CallAdapter.Factory() {
    companion object {
        fun create() = ApiResponseAdapterFactory()
    }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        check(returnType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported" }

        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != ApiResponse::class.java) {
            return null
        }

        check(responseType is ParameterizedType) { "$responseType must be parameterized. Raw types are not supported" }

        val bodyType = getParameterUpperBound(0, responseType)
        val errorBodyConverter =
            retrofit.nextResponseBodyConverter<Any>(null, bodyType, annotations)

        return if (Call::class.java != getRawType(returnType)) {
            null
        } else {
            ApiResponseAdapter(bodyType, errorBodyConverter)
        }
    }
}