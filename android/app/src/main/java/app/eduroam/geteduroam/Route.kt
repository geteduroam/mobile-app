package app.eduroam.geteduroam

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.eduroam.geteduroam.config.AndroidConfigParser
import app.eduroam.geteduroam.config.model.EAPIdentityProvider
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.extensions.DateJsonAdapter
import app.eduroam.geteduroam.models.Configuration
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.io.StringReader
import java.net.URL
import java.util.Date


sealed class Route(val route: String) {
    object SelectInstitution : Route(route = "select_institution")
    object SelectProfile : Route(route = "select_profile") {
        const val institutionIdArg = "institutionIdArg"
        const val customHostArg = "customHostArg"
        val routeWithArgs = "$route/?institutionId={$institutionIdArg}&customHost={$customHostArg}"
        val arguments = listOf(
            navArgument(institutionIdArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            },
            navArgument(customHostArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            }
        )
        val deepLinkUrl = "$BASE_URI/${route}/?institutionId={${institutionIdArg}}&customHost={$customHostArg}"
        fun buildDeepLink(institutionId: String) = "$BASE_URI/${route}/?institutionId=${Uri.encode(institutionId)}"
        fun buildDeepLink(customHost: Uri) = "$BASE_URI/${route}/?customHost=${Uri.encode(customHost.toString())}"
        fun encodeInstitutionIdArgument(id: String) = "$route/?institutionId=${Uri.encode(id)}"
        fun encodeCustomHostArgument(customHost: Uri) = "$route/?customHost=${Uri.encode(customHost.toString())}"
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
        const val organizationIdArg = "organizationid"
        const val wifiConfigDataArg = "wificonfigdata"
        const val emptyOrganization = "no_organization_id"
        val routeWithArgs = "$route/{$wifiConfigDataArg}?organization={$organizationIdArg}"
        val arguments = listOf(
            navArgument(organizationIdArg) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(wifiConfigDataArg) {
                type = NavType.StringType
                defaultValue = ""
            })

        val deepLinkUrl = "$BASE_URI/$route/{$wifiConfigDataArg}?organization={$organizationIdArg}"
        suspend fun buildDeepLink(context: Context, fileUri: Uri): String? {
            // Read the contents of the file as XML
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
            val bytes = inputStream.readBytes()
            val configParser = AndroidConfigParser()
            return try {
                val provider = configParser.parse(bytes)
                inputStream.close()
                "${BASE_URI}/${encodeArguments(null, provider)}"
            } catch (ex: Exception) {
                Timber.w(ex, "Could not parse file opened!")
                null
            }
        }


        fun encodeArguments(organizationId: String?, eapIdentityProviderList: EAPIdentityProviderList): String {
            val moshi = Moshi.Builder()
                .add(Date::class.java, DateJsonAdapter())
                .build()
            // Here we remove all the embedded images. This is required because some profiles embed images of several megabytes,
            // which makes the app slow or even crash
            val listWithoutLogos = eapIdentityProviderList.copy(
                eapIdentityProvider = eapIdentityProviderList.eapIdentityProvider?.map { provider ->
                    provider.copy(providerInfo = provider.providerInfo?.copy(providerLogo = null))
                }
            )
            val adapter: JsonAdapter<EAPIdentityProviderList> = moshi.adapter(EAPIdentityProviderList::class.java)
            val wifiConfigDataJson = adapter.toJson(listWithoutLogos)
            val encodedWifiConfig = Uri.encode(wifiConfigDataJson)
            return if (organizationId.isNullOrEmpty()) {
                "$route/$encodedWifiConfig"
            } else {
                "$route/$encodedWifiConfig?organization=$organizationId"
            }
        }

        fun decodeOrganizationIdArgument(arguments: Bundle?): String {
            return arguments?.getString(organizationIdArg).orEmpty()
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