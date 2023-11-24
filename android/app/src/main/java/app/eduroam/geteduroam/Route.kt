package app.eduroam.geteduroam

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.extensions.DateJsonAdapter
import app.eduroam.geteduroam.models.Configuration
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Date


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
        const val configurationArg = "configurationArg"

        val routeWithArgs = "$route?config={$configurationArg}"
        val arguments = listOf(
            navArgument(configurationArg) {
                type = NavType.StringType
                nullable = false
                defaultValue = ""
            }
        )

        fun encodeArguments(configuration: Configuration): String {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<Configuration> = moshi.adapter(Configuration::class.java)
            val serializedConfig = adapter.toJson(configuration)
            val encodedConfig = Uri.encode(serializedConfig)
            return "$route?config=$encodedConfig"
        }

        fun decodeUrlArgument(encodedConfiguration: String): Configuration {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<Configuration> = moshi.adapter(Configuration::class.java)
            val decodedConfiguration = Uri.decode(encodedConfiguration)
            return adapter.fromJson(decodedConfiguration)!!
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
            val moshi = Moshi.Builder()
                .add(Date::class.java, DateJsonAdapter())
                .build()
            val adapter: JsonAdapter<EAPIdentityProviderList> = moshi.adapter(EAPIdentityProviderList::class.java)
            val wifiConfigDataJson = adapter.toJson(eapIdentityProviderList)
            val encodedWifiConfig = Uri.encode(wifiConfigDataJson)
            return "$route/$encodedWifiConfig"
        }

        fun decodeUrlArgument(arguments: Bundle?): EAPIdentityProviderList {
            val encodedEAPIdentityProviderList = arguments?.getString(wifiConfigDataArg).orEmpty()
            val moshi = Moshi.Builder()
                .add(Date::class.java, DateJsonAdapter())
                .build()
            val adapter: JsonAdapter<EAPIdentityProviderList> = moshi.adapter(EAPIdentityProviderList::class.java)
            val decodedWifiConfigDataJson = Uri.decode(encodedEAPIdentityProviderList)
            return adapter.fromJson(decodedWifiConfigDataJson)!!
        }
    }
}