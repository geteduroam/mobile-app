package app.eduroam.shared.ktor

import app.eduroam.shared.response.InstitutionResult

interface InstitutionApi {
    suspend fun getJsonFromApi(): InstitutionResult
}