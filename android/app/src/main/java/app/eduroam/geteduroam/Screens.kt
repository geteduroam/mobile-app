package app.eduroam.geteduroam

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screens(val route: String) {
    object SelectProfile : Screens(route = "select_profile") {
        const val urlArg = "url"
        val routeWithArgs = "${route}/{${urlArg}}"
        val arguments = listOf(navArgument(urlArg) {
            type = NavType.StringType
            defaultValue = ""
        })
    }

    object SelectInstitution : Screens(route = "select_institution")
}