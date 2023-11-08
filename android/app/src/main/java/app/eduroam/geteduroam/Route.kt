package app.eduroam.geteduroam

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.net.URLDecoder
import java.net.URLEncoder


sealed class Route(val route: String) {
    object SelectInstitution : Route(route = "select_institution")
    object SelectProfile : Route(route = "select_profile") {
        const val institutionIdArg = "institutionIdArg"
        val routeWithArgs = "$route/{$institutionIdArg}"
        val arguments = listOf(
            navArgument(institutionIdArg) {
                type = NavType.StringType
                nullable = false
                defaultValue = ""
            },
        )

        fun encodeArgument(id: String) =
            "$route/${URLEncoder.encode(id, Charsets.UTF_8.toString())}"
    }

    object OAuth : Route(route = "oauth_prompt") {
        const val authEndpointArg = "authEndpointArg"
        const val tokenEndpointArg = "tokenEndpointArg"

        val routeWithArgs = "$route/{$authEndpointArg}/{$tokenEndpointArg}"
        val arguments = listOf(
            navArgument(authEndpointArg) {
                type = NavType.StringType
                nullable = false
                defaultValue = ""
            },
            navArgument(tokenEndpointArg) {
                type = NavType.StringType
                nullable = false
                defaultValue = ""
            },
        )

        fun encodeArguments(authEndpoint: String, tokenEndpoint: String): String {
            val authEndpointEncoded = URLEncoder.encode(authEndpoint, Charsets.UTF_8.toString())
            val tokenEndpointEndcoded = URLEncoder.encode(tokenEndpoint, Charsets.UTF_8.toString())
            return "$route/$authEndpointEncoded/$tokenEndpointEndcoded"
        }

    }

    object ConfigureWifi : Route(route = "configure_wifi") {
        const val wifiConfigDataArg = "wificonfigdata"
        val routeWithArgs = "${route}/{${wifiConfigDataArg}}"
        val arguments = listOf(navArgument(wifiConfigDataArg) {
            type = NavType.StringType
            defaultValue = ""
        })

        fun encodeArguments(eapIdentityProviderList: EAPIdentityProviderList): String {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<EAPIdentityProviderList> = moshi.adapter(EAPIdentityProviderList::class.java)
            val wifiConfigDataJson = adapter.toJson(eapIdentityProviderList)
            val encodedWifiConfig = Uri.encode(wifiConfigDataJson)
            return "$route/$encodedWifiConfig"
        }

        fun decodeUrlArgument(arguments: Bundle?): EAPIdentityProviderList {
            val encodedEAPIdentityProviderList = arguments?.getString(wifiConfigDataArg).orEmpty()
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<EAPIdentityProviderList> = moshi.adapter(EAPIdentityProviderList::class.java)
            val decodedWifiConfigDataJson = Uri.decode(encodedEAPIdentityProviderList)
            return adapter.fromJson(decodedWifiConfigDataJson)!!
        }
    }
}