package app.eduroam.geteduroam

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
            Log.d(
                "ScreensNav",
                "$route : decoded URL: $decodedUrl"
            )
            return decodedUrl
        }

        fun decodeProfileArgument(arguments: Bundle?): Profile {
            val encodedProfile = arguments?.getString(profileArg).orEmpty()
            val profileJson = URLDecoder.decode(encodedProfile, Charsets.UTF_8.toString())
            return Json.decodeFromString(profileJson)
        }

        fun encodeArguments(url: String, profile: Profile): String {
            Log.d(
                "ScreensNav",
                "$route : encodeArguments() called with: url = $url, profile = $profile"
            )
            val encodedUrl = URLEncoder.encode(url, Charsets.UTF_8.toString())
            val encodedProfile =
                URLEncoder.encode(Json.encodeToString(profile), Charsets.UTF_8.toString())
            return "$route/$encodedUrl/$encodedProfile"
        }

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