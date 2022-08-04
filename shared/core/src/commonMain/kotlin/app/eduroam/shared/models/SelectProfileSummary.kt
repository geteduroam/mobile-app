package app.eduroam.shared.models

import app.eduroam.shared.response.Profile

data class SelectProfileSummary(val profiles: List<Profile>, val selectedProfile: Profile?)