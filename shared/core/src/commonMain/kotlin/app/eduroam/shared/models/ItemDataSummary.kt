package app.eduroam.shared.models

import app.eduroam.geteduroam.models.Institution

data class ItemDataSummary(val institutions: List<Institution>, val filterOn: String = "")