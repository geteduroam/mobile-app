package app.eduroam.geteduroam

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.eduroam.geteduroam.config.AndroidConfigParser
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.extensions.DateJsonAdapter
import app.eduroam.geteduroam.models.Configuration
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.io.StringReader
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
        val deepLinkUrl = "$BASE_URI/${route}/{${institutionIdArg}}"
        fun buildDeepLink(institutionId: String) =  "$BASE_URI/${route}/${Uri.encode(institutionId)}"
        fun encodeArgument(id: String) = "$route/${Uri.encode(id)}"
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

        val deepLinkUrl = "$BASE_URI/$route/{${wifiConfigDataArg}}"
        suspend fun buildDeepLink(context: Context, fileUri: Uri) : String? {
            // Read the contents of the file as XML
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
            val bytes = inputStream.readBytes()
            val configParser = AndroidConfigParser()
            return try {
                val provider = configParser.parse(bytes)
                inputStream.close()
                "${BASE_URI}/${encodeArguments(provider)}"
            } catch (ex: Exception) {
                Timber.e(ex, "Could not parse file opened!")
                null
            }
        }


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