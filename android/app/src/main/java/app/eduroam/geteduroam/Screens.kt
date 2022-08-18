package app.eduroam.geteduroam

import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.eduroam.shared.response.Institution
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screens(val route: String) {
    object SelectProfile : Screens(route = "select_profile") {
        const val institutionArg = "institution"
        val routeWithArgs = "${route}/{${institutionArg}}"
        val arguments = listOf(navArgument(institutionArg) {
            type = NavType.StringType
            defaultValue = ""
        })

        fun encodeArguments(institutionJson: String): String = route + (
                "/${
                    URLEncoder.encode(
                        institutionJson, Charsets.UTF_8.toString()
                    )
                }"
                )

        fun decodeUrlArgument(arguments: Bundle?): Institution {
            val encodedInstitution = arguments?.getString(institutionArg).orEmpty()
            val decodedInstitutionJson =
                URLDecoder.decode(encodedInstitution, Charsets.UTF_8.toString())
            return Json.decodeFromString<Institution>(decodedInstitutionJson)
        }
    }

    object SelectInstitution : Screens(route = "select_institution")
}