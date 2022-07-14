package app.eduroam.shared.models


import app.eduroam.shared.select.SelectInstitutionRepository
import app.eduroam.shared.select.SelectInstitutionViewModel
import co.touchlab.kermit.Logger
import app.eduroam.shared.models.CallbackViewModel

@Suppress("Unused") // Members are called from Swift
class SelectInstitutionCallbackViewModel(
    private val institutionRepository: SelectInstitutionRepository,
    log: Logger,
) : CallbackViewModel() {

    override val viewModel = SelectInstitutionViewModel(institutionRepository, log)

    val institutions = viewModel.institutions.asCallbacks()
}
