package app.eduroam.shared.select

import app.eduroam.shared.ktor.InstitutionApi
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.ItemDataSummary
import app.eduroam.shared.response.TokenResponse
import app.eduroam.shared.storage.DriverFactory
import app.eduroam.shared.storage.createDatabase
import co.touchlab.kermit.Logger
import co.touchlab.stately.ensureNeverFrozen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class InstitutionsRepository(
    private val institutionApi: InstitutionApi,
    driverFactory: DriverFactory,
    log: Logger,
) {

    private val log = log.withTag("InstitutionsRepository")
    private val database = createDatabase(driverFactory)

    init {
        ensureNeverFrozen()
    }

    fun fetchInstitutions(): Flow<DataState<ItemDataSummary>> = flow {
        emit(DataState(loading = true))
        val institutionsList: DataState<ItemDataSummary> = getInstitutionsFromNetwork()
        emit(institutionsList)
    }

    suspend fun postToken(
        tokenUrl: String,
        code: String,
        redirectUri: String,
        clientId: String,
        codeVerifier: String
    ): TokenResponse =
        institutionApi.postToken(tokenUrl, code, redirectUri, clientId, codeVerifier)

    suspend fun getEapData(
        id: String,
        profileId: String,
        eapconfigEndpoint: String,
        accessToken: String? = null
    ): ByteArray =
        database.eduroamdbQueries.getEapFile(categoryId = id, profileId = profileId)
            .executeAsOneOrNull() ?: downloadEapFile(id, profileId, eapconfigEndpoint, accessToken)

    private suspend fun downloadEapFile(
        id: String,
        profileId: String,
        eapconfigEndpoint: String,
        accessToken: String? = null
    ): ByteArray {
        val byteArray = institutionApi.downloadEapFile(eapconfigEndpoint, accessToken)

        database.eduroamdbQueries.saveEapFile(
            categoryId = id,
            profileId = profileId,
            eapFile = byteArray,
            lastDownloadTimestamp = Clock.System.now().toEpochMilliseconds()
        )
        return byteArray
    }

    private suspend fun getInstitutionsFromNetwork(): DataState<ItemDataSummary> = try {
        val institutionResult = institutionApi.getJsonFromApi()
        log.v { "Institutions network result: ${institutionResult.instances.size}" }
        if (institutionResult.instances.isEmpty()) {
            DataState(empty = true)
        } else {
            DataState(
                ItemDataSummary(
                    institutionResult.instances
                )
            )
        }
    } catch (e: Exception) {
        log.e(e) { "Error fetching institutions list" }
        DataState(exception = "Unable to download institutions list")
    }
}