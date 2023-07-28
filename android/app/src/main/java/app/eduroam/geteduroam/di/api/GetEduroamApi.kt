package app.eduroam.geteduroam.di.api

import app.eduroam.geteduroam.models.InstitutionResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface GetEduroamApi {
    @GET("v1/discovery.json")
    suspend fun getInstitutions(): Response<InstitutionResult>

    @POST
    suspend fun downloadEapFile(
        @Url eapconfigEndpoint: String,
        @Header("Authorization") accessToken: String?,
    ): ByteArray
}