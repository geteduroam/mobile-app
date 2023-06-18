package app.eduroam.geteduroam.institutions

import app.eduroam.geteduroam.models.Institution
import app.eduroam.geteduroam.ui.ErrorData

data class UiState(
    val institutions: List<Institution> = emptyList(),
    val filter: String = "",
    val isLoading: Boolean = false,
    val selectedInstitution: Institution? = null,
    val promptAuth: Unit? = null,
    val errorData: ErrorData? = null,
)