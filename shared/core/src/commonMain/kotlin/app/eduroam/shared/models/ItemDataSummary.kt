package app.eduroam.shared.models

import app.eduroam.shared.response.Institution

data class ItemDataSummary(val institutions: List<Institution>, val filterOn: String = "")