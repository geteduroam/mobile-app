package app.eduroam.geteduroam

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.eduroam.shared.config.WifiConfigData
import app.eduroam.shared.response.Institution
import app.eduroam.shared.response.Profile
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screens(val route: String) {

    object OAuth : Screens(route = "OAuthRedirect") {
        const val APP_ID = "app.eduroam.geteduroam"
        val redirectUrl = "$APP_ID:/".encodeURLParameter()
        private const val urlArg = "url"
        private const val profileArg = "profile"

        val routeWithArgs = "${route}/{${urlArg}}/{${profileArg}}"
        val arguments = listOf(
            navArgument(urlArg) {
                type = NavType.StringType
                defaultValue = Uri.EMPTY.toString()
            },
            navArgument(profileArg) {
                type = NavType.StringType
                defaultValue = ""
            },
        )

        fun decodeUrlArgument(arguments: Bundle?): String {
            val encodedUrl = arguments?.getString(urlArg).orEmpty()
            val decodedUrl = URLDecoder.decode(encodedUrl, Charsets.UTF_8.toString())
            return decodedUrl
        }

        fun decodeProfileArgument(arguments: Bundle?): Profile {
            val profileJson = arguments?.getString(profileArg).orEmpty()
            return Json.decodeFromString<Profile>(profileJson)
        }

        fun encodeArguments(url: String, profile: Profile): String = route + ("/${
            URLEncoder.encode(
                url, Charsets.UTF_8.toString()
            )
        }") + ("/${
            Json.encodeToString(profile)
        }")

    }

    object SelectProfile : Screens(route = "select_profile") {
        const val institutionArg = "institution"
        val routeWithArgs = "${route}/{${institutionArg}}"
        val arguments = listOf(navArgument(institutionArg) {
            type = NavType.StringType
            defaultValue = ""
        })

        fun encodeArguments(institution: Institution): String {
            val institutionJson = Json.encodeToString(institution)
            val instutitionArg = URLEncoder.encode(
                institutionJson, Charsets.UTF_8.toString()
            )
            return "$route/$instutitionArg"
        }

        fun decodeUrlArgument(arguments: Bundle?): Institution {
            val encodedInstitution = arguments?.getString(institutionArg).orEmpty()
            val decodedInstitutionJson =
                URLDecoder.decode(encodedInstitution, Charsets.UTF_8.toString())
            return Json.decodeFromString(decodedInstitutionJson)
        }
    }

    object ConfigureWifi : Screens(route = "configure_wifi") {
        const val wifiConfigDataArg = "wificonfigdata"
        val routeWithArgs = "${route}/{${wifiConfigDataArg}}"
        val arguments = listOf(navArgument(wifiConfigDataArg) {
            type = NavType.StringType
            defaultValue = ""
        })

        fun encodeArguments(wifiConfigData: WifiConfigData): String {
            val wifiConfigDataJson = Json.encodeToString(wifiConfigData)
            val encodedWifiConfig = URLEncoder.encode(
                wifiConfigDataJson, Charsets.UTF_8.toString()
            )
            return "$route/$encodedWifiConfig"
        }

        fun decodeUrlArgument(arguments: Bundle?): WifiConfigData {
            val encodedWifiConfigData = arguments?.getString(wifiConfigDataArg).orEmpty()
            val decodedWifiConfigDataJson =
                URLDecoder.decode(encodedWifiConfigData, Charsets.UTF_8.toString())
            return Json.decodeFromString(decodedWifiConfigDataJson)
        }
    }

    object SelectInstitution : Screens(route = "select_institution")
}