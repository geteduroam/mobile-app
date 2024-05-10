package app.eduroam.geteduroam.di.api

import app.eduroam.geteduroam.models.DiscoveryResult
import retrofit2.Response
import retrofit2.http.GET

interface GetEduroamApi {
    @GET("v2/discovery.json")
    suspend fun discover(): Response<DiscoveryResult>
}