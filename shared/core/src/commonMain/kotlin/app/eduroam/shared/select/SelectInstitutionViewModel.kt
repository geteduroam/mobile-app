package app.eduroam.shared.select

import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.ItemDataSummary
import app.eduroam.shared.models.ViewModel
import app.eduroam.shared.response.Institution
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SelectInstitutionViewModel(
    private val institutionRepository: SelectInstitutionRepository,
    log: Logger,
) : ViewModel() {

    private val log = log.withTag("SelectInstitutionViewModel")

    val institutions: StateFlow<DataState<ItemDataSummary>> = MutableStateFlow(
        DataState(loading = true)
    )

    init {
        observeInstitutions()
    }

    override fun onCleared() {
        log.v("Clearing SelectInstitutionViewModel")
    }


    fun onInstitutionSelect(selectedInstitution: Institution) {
        //TODO: handle selection
    }

    private fun observeInstitutions() {
        viewModelScope.launch {
            log.v { "getInstitutionsList: Collecting Things" }
            institutionRepository.fetchInstitutions().collect { dataState ->
                if (dataState.loading) {
                    updateInstitutions(institutions.value.copy(loading = true))
                } else {
                    updateInstitutions(dataState)
                }
            }
        }
    }

    private fun updateInstitutions(newValue: DataState<ItemDataSummary>) {
        (institutions as MutableStateFlow).value = newValue
    }
}
