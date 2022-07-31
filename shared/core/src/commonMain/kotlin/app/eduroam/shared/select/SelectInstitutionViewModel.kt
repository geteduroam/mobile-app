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
    private val institutionRepository: InstitutionsRepository,
    log: Logger,
) : ViewModel() {

    private val log = log.withTag("SelectInstitutionViewModel")

    val uiDataState: StateFlow<DataState<ItemDataSummary>> = MutableStateFlow(
        DataState(loading = true)
    )

    val currentInstitution: MutableStateFlow<Institution?> = MutableStateFlow(null)

    init {
        fetchInstitutionsList()
    }

    private fun fetchInstitutionsList() {
        viewModelScope.launch {
            institutionRepository.fetchInstitutions().collect { dataState ->
                if (dataState.loading) {
                    updateDataState(uiDataState.value.copy(loading = true))
                } else {
                    updateDataState(dataState)
                }
            }
        }
    }

    override fun onCleared() {
        log.v("Clearing SelectInstitutionViewModel")
    }

    fun onInstitutionSelect(selectedInstitution: Institution) {
        if (selectedInstitution.hasSingleProfile()) {
            updateDataState(uiDataState.value.copy(loading = true))
            //todo: download EAP file
        } else {
            currentInstitution.value = selectedInstitution
        }
    }

    private fun updateDataState(newValue: DataState<ItemDataSummary>) {
        (uiDataState as MutableStateFlow).value = newValue
    }

    fun onSearchTextChange(search: String) {
        val listData = uiDataState.value.data ?: return
        updateDataState(DataState(listData.copy(filterOn = search)))
        if (search.length >= 3) {
            val filteredList = listData.institutions.filter { it.name.startsWith(search, true) || it.name.contains(search, true) }
            updateDataState(DataState(listData.copy(filterOn = search,
                institutions = filteredList)))
        }
    }
}
