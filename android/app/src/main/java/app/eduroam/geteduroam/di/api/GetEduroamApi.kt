package app.eduroam.geteduroam.di.api

import app.eduroam.geteduroam.models.OrganizationResult
import retrofit2.Response
import retrofit2.http.GET

interface GetEduroamApi {
    @GET("v1/discovery.json")
    suspend fun getOrganizations(): Response<OrganizationResult>
}