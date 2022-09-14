package app.eduroam.shared.select

import app.eduroam.shared.config.ConfigParser
import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.ItemDataSummary
import app.eduroam.shared.models.ViewModel
import app.eduroam.shared.response.Institution
import app.eduroam.shared.response.Profile
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SelectInstitutionViewModel(
    private val institutionRepository: InstitutionsRepository,
    private val configParser: ConfigParser,
    log: Logger,
) : ViewModel() {

    private val log = log.withTag("SelectInstitutionViewModel")

    val uiDataState: StateFlow<DataState<ItemDataSummary>> = MutableStateFlow(
        DataState(loading = true)
    )

    val currentInstitution: MutableStateFlow<Institution?> = MutableStateFlow(null)

    //There is no end point that allows searching so we have to get all the institutions once and cache the result
    lateinit var allInstitutions: List<Institution>

    init {
        fetchInstitutionsList()
    }

    private fun fetchInstitutionsList() {
        viewModelScope.launch {
            institutionRepository.fetchInstitutions().collect { dataState ->
                if (dataState.loading) {
                    updateDataState(uiDataState.value.copy(loading = true))
                } else {
                    allInstitutions = dataState.data?.institutions ?: emptyList()
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
            val profile = selectedInstitution.profiles[0]
            if (selectedInstitution.requiresAuth(profile)) {
                //TODO: start OAuth flow
            } else {
                updateDataState(uiDataState.value.copy(loading = true))
                viewModelScope.launch {
                    try {
                        val eapData = institutionRepository.getEapData(
                            selectedInstitution.id, profile.id, profile.eapconfig_endpoint.orEmpty()
                        )
                        configParser.parse(eapData)
                    } catch (e: Exception) {
                        log.e("Failed to download anon EAP config file", e)
                    } finally {
                        updateDataState(uiDataState.value.copy(loading = false))
                    }
                }
            }
        } else {
            currentInstitution.value = selectedInstitution
        }
    }

    private fun updateDataState(newValue: DataState<ItemDataSummary>) {
        (uiDataState as MutableStateFlow).value = newValue
    }

    fun onSearchTextChange(search: String) {
        val listData: ItemDataSummary = uiDataState.value.data ?: return
        updateDataState(DataState(listData.copy(filterOn = search)))
        searchOnFilter(search, listData)
    }

    private fun searchOnFilter(
        search: String, listData: ItemDataSummary
    ) {
        if (search.length >= 3) {
            val filteredList = allInstitutions.filter {
                it.name.startsWith(search, true) || it.name.contains(
                    search, true
                )
            }
            updateDataState(
                DataState(
                    listData.copy(
                        filterOn = search, institutions = filteredList
                    )
                )
            )
        }
    }

    private fun searchOnMultipleProfiles(
        search: String, listData: ItemDataSummary
    ) {
        val filteredList = allInstitutions.filter {
            it.profiles.size > 1
        }
        updateDataState(
            DataState(
                listData.copy(
                    filterOn = search, institutions = filteredList
                )
            )
        )
    }

    fun clearCurrentInstitutionSelection() {
        currentInstitution.value = null
    }
}
