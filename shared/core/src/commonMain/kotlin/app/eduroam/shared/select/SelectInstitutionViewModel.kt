package app.eduroam.shared.select

import app.eduroam.shared.OAuth2
import app.eduroam.shared.config.ConfigParser
import app.eduroam.shared.config.WifiConfigData
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
    private val configParser: ConfigParser,
    log: Logger,
) : ViewModel() {
    val step: MutableStateFlow<Step> = MutableStateFlow(Step.Start)
    val uiDataState: StateFlow<DataState<ItemDataSummary>> = MutableStateFlow(
        DataState(loading = true)
    )

    val creds: MutableStateFlow<Pair<String?,String?>> = MutableStateFlow(Pair<String?,String?>(null, null))

    //There is no end point that allows searching so we have to get all the institutions once and cache the result
    private lateinit var allInstitutions: List<Institution>
    private val log = log.withTag("SelectInstitutionViewModel")

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

    fun clearDialog() {
        (uiDataState as MutableStateFlow).value = uiDataState.value.copy(showDialog = false)
    }

    override fun onCleared() {
        log.v("Clearing SelectInstitutionViewModel")
    }

    fun onInstitutionSelect(
        selectedInstitution: Institution, redirectUri: String, clientId: String
    ) {
        if (selectedInstitution.hasSingleProfile()) {
            log.d("Single profile for institution")
            if (!selectedInstitution.requiresAuth()) {
                log.d("No OAuth required for the single profile")
                val profile = selectedInstitution.profiles.first()
                updateDataState(uiDataState.value.copy(loading = true))
                viewModelScope.launch {
                    try {
                        val eapData = institutionRepository.getEapData(
                            selectedInstitution.id, profile.id, profile.eapconfig_endpoint.orEmpty()
                        )
                        step.value = Step.DoConfig(configParser.parse(eapData))
                    } catch (e: Exception) {
                        log.e("Failed to download anon EAP config file", e)
                    } finally {
                        updateDataState(uiDataState.value.copy(loading = false))
                    }
                }
            } else if (selectedInstitution.requiresAuth()) {
                log.d("OAuth required for the single profile available")
                step.value = Step.DoOAuthFor(
                    selectedInstitution.profiles[0], OAuth2().getAuthorizationUrl(
                        selectedInstitution.id,
                        selectedInstitution.profiles[0].authorization_endpoint,
                        redirectUri,
                        clientId
                    )
                )
            }
        } else {
            log.d("Must first select profile")
            step.value = Step.PickProfileFrom(selectedInstitution)
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

    fun onStepCompleted() {
        step.value = Step.Start
    }

    private val fakeProfileData = """
       {
            "cat_idp": 1,
			"country" : "ES",
            "id": "fakeid",
			"name" : "Fake OAuth",
			"profiles" : [
				{
					"id" : "fake_no",
					"name" : "Fake OAuth",
					"eapconfig_endpoint" : "https://geteduroam.no/generate.php",
					"token_endpoint" : "https://geteduroam.no/token.php",
					"authorization_endpoint" : "https://geteduroam.no/authorize.php",
					"oauth" : true
				},
				{
					"id" : "fake2_no",
					"name" : "Fake OAuth2",
					"eapconfig_endpoint" : "https://geteduroam.no/generate.php",
					"token_endpoint" : "https://geteduroam.no/token.php",
					"authorization_endpoint" : "https://geteduroam.no/authorize.php",
					"oauth" : true
				}
			]
		} 
    """.trimIndent()
}
