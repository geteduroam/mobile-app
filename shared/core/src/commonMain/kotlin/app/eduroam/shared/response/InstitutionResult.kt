package app.eduroam.shared.response

import kotlinx.serialization.Serializable

@Serializable
data class InstitutionResult(
    val instances: List<Institution>,
    val version: Int,
)
