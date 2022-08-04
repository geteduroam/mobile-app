package app.eduroam.shared.profile

import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.SelectProfileSummary
import app.eduroam.shared.models.ViewModel
import app.eduroam.shared.response.Institution
import app.eduroam.shared.response.Profile
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SelectProfileViewModel(
    log: Logger,
) : ViewModel() {

    private val log = log.withTag("SelectProfileViewModel")

    val uiDataState: StateFlow<DataState<SelectProfileSummary>> = MutableStateFlow(
        DataState(loading = true)
    )

    val currentProfile: MutableStateFlow<Profile?> = MutableStateFlow(null)


    override fun onCleared() {
        log.v("Clearing SelectInstitutionViewModel")
    }

    fun profilesForInstitution(institutionJson: String) {
        val institution = Json.decodeFromString<Institution>(institutionJson)
        updateDataState(DataState(SelectProfileSummary(institution.profiles, null), loading = false))
    }

    fun onProfileSelect(selectedProfile: Profile) {
        currentProfile.value = selectedProfile
        //todo: handle authentication if required
    }

    private fun updateDataState(newValue: DataState<SelectProfileSummary>) {
        (uiDataState as MutableStateFlow).value = newValue
    }
}
