package app.eduroam.shared.select

import app.eduroam.shared.ktor.InstitutionApi
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.ItemDataSummary
import co.touchlab.kermit.Logger
import co.touchlab.stately.ensureNeverFrozen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InstitutionsRepository(
    private val institutionApi: InstitutionApi,
    log: Logger,
) {

    private val log = log.withTag("InstitutionsRepository")

    init {
        ensureNeverFrozen()
    }

    fun fetchInstitutions(): Flow<DataState<ItemDataSummary>> = flow {
        emit(DataState(loading = true))
        val institutionsList: DataState<ItemDataSummary> = getInstitutionsFromNetwork()
        emit(institutionsList)
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