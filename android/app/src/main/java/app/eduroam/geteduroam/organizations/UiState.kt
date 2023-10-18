package app.eduroam.geteduroam.organizations

import app.eduroam.geteduroam.models.Organization
import app.eduroam.geteduroam.ui.ErrorData

data class UiState(
    val organizations: List<Organization> = emptyList(),
    val filter: String = "",
    val isLoading: Boolean = false,
    val selectedOrganization: Organization? = null,
    val promptAuth: Unit? = null,
    val errorData: ErrorData? = null,
)