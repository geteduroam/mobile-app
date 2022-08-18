package app.eduroam.shared.profile

import app.eduroam.shared.models.DataState
import app.eduroam.shared.models.SelectProfileSummary
import app.eduroam.shared.models.ViewModel
import app.eduroam.shared.response.Institution
import app.eduroam.shared.response.Profile
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SelectProfileViewModel(
    log: Logger,
) : ViewModel() {

    private val log = log.withTag("SelectProfileViewModel")

    val uiDataState: StateFlow<DataState<SelectProfileSummary>> = MutableStateFlow(
        DataState(loading = true)
    )


    override fun onCleared() {
        log.v("Clearing SelectInstitutionViewModel")
    }

    fun profilesForInstitution(institution: Institution) {
        updateDataState(
            DataState(
                SelectProfileSummary(institution.profiles, null), loading = false
            )
        )
    }

    fun onSelectProfile(selectedProfile: Profile) {
        updateDataState(uiDataState.value.copy(loading = true))
        //todo: handle authentication if required
    }

    private fun updateDataState(newValue: DataState<SelectProfileSummary>) {
        (uiDataState as MutableStateFlow).value = newValue
    }
}
