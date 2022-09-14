package app.eduroam.shared.ktor

import app.eduroam.shared.response.InstitutionResult
import app.eduroam.shared.response.TokenResponse

interface InstitutionApi {
    suspend fun getJsonFromApi(): InstitutionResult
    suspend fun downloadEapFile(eapconfigEndpoint: String, accessToken: String? = null): ByteArray

    suspend fun postToken(
        tokenUrl: String,
        code: String,
        redirectUri: String,
        clientId: String,
        codeVerifier: String
    ): TokenResponse
}