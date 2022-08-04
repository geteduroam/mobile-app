package app.eduroam.geteduroam

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screens(val route: String) {
    object SelectProfile : Screens(route = "select_profile") {
        const val institution = "institution"
        val routeWithArgs = "${route}/{${institution}}"
        val arguments = listOf(navArgument(institution) {
            type = NavType.StringType
            defaultValue = ""
        })
    }

    object SelectInstitution : Screens(route = "select_institution")
}