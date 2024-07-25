package app.eduroam.geteduroam

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.eduroam.geteduroam.config.AndroidConfigParser
import app.eduroam.geteduroam.config.model.EAPIdentityProvider
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.extensions.DateJsonAdapter
import app.eduroam.geteduroam.models.Configuration
import app.eduroam.geteduroam.status.ConfigSource
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.io.StringReader
import java.net.URL
import java.util.Date


sealed class Route(val route: String) {
    object SelectInstitution : Route(route = "select_institution")
    object StatusScreen : Route(route = "status_screen")
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
        const val redirectUriArg = "redirectUriArg"

        val routeWithArgs = "$route?config={$configurationArg}&redirectUri={$redirectUriArg}"
        val arguments = listOf(
            navArgument(configurationArg) {
                type = NavType.StringType
                nullable = false
                defaultValue = ""
            },
            navArgument(redirectUriArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            }
        )

        fun encodeArguments(configuration: Configuration, redirectUri: Uri?): String {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<Configuration> = moshi.adapter(Configuration::class.java)
            val serializedConfig = adapter.toJson(configuration)
            val encodedConfig = Uri.encode(serializedConfig)
            if (redirectUri == null) {
                return "$route?config=$encodedConfig"
            } else {
                val encodedUri = Uri.encode(redirectUri.toString())
                return "$route?config=$encodedConfig&redirectUri=$encodedUri"
            }
        }

        fun decodeConfigurationArgument(encodedConfiguration: String): Configuration {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<Configuration> = moshi.adapter(Configuration::class.java)
            val decodedConfiguration = Uri.decode(encodedConfiguration)
            return adapter.fromJson(decodedConfiguration)!!
        }
    }

    object WebViewFallback : Route(route = "webview_fallback") {
        const val urlArg = "urlArg"
        const val configurationArg = "configurationArg"

        val routeWithArgs = "$route/{$configurationArg}?url={$urlArg}"
        val arguments = listOf(
            navArgument(configurationArg) {
                type = NavType.StringType
                nullable = false
                defaultValue = ""
            },
            navArgument(urlArg) {
                type = NavType.StringType
                nullable = false
                defaultValue = ""
            }
        )

        fun decodeConfigurationArgument(encodedConfiguration: String): Configuration {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<Configuration> = moshi.adapter(Configuration::class.java)
            val decodedConfiguration = Uri.decode(encodedConfiguration)
            return adapter.fromJson(decodedConfiguration)!!
        }

        fun encodeArguments(configuration: Configuration, uri: Uri): String {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<Configuration> = moshi.adapter(Configuration::class.java)
            val serializedConfig = adapter.toJson(configuration)
            val encodedConfig = Uri.encode(serializedConfig)
            val encodedUri = Uri.encode(uri.toString())
            return "$route/$encodedConfig?url=$encodedUri"
        }
    }


    object ConfigureWifi : Route(route = "configure_wifi") {
        const val organizationIdArg = "organizationId"
        const val organizationNameArg = "organizationName"
        const val wifiConfigDataArg = "wificonfigdata"
        const val sourceArg = "source"
        const val emptyOrganization = "no_organization_id"
        val routeWithArgs = "$route/{$wifiConfigDataArg}?organizationId={$organizationIdArg}&source=${sourceArg}&organizationName={$organizationNameArg}"
        val arguments = listOf(
            navArgument(organizationIdArg) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(wifiConfigDataArg) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(sourceArg) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(organizationNameArg) {
                type = NavType.StringType
                nullable = true
            }
        )

        val deepLinkUrl =
            "$BASE_URI/$route/{$wifiConfigDataArg}?organizationId={$organizationIdArg}&source=${sourceArg}&organizationName={$organizationNameArg}"

        suspend fun buildDeepLink(context: Context, fileUri: Uri): String? {
            // Read the contents of the file as XML
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
            val bytes = inputStream.readBytes()
            val configParser = AndroidConfigParser()
            return try {
                val provider = configParser.parse(bytes)
                inputStream.close()
                "${BASE_URI}/${encodeArguments(ConfigSource.File, null, null, provider)}"
            } catch (ex: Exception) {
                Timber.w(ex, "Could not parse file opened!")
                null
            }
        }

        fun encodeArguments(
            source: ConfigSource,
            organizationId: String?,
            organizationName: String?,
            eapIdentityProviderList: EAPIdentityProviderList
        ): String {
            // TODO use type-safe navigation because this is something crashing, and we don't know why
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
            val baseRoute = if (organizationId.isNullOrEmpty()) {
                "$route/$encodedWifiConfig?source=${source.name}&organizationName="
            } else {
                "$route/$encodedWifiConfig?organizationId=$organizationId&source=${source.name}&organizationName="
            }
            return baseRoute + Uri.encode(organizationName ?: eapIdentityProviderList.eapIdentityProvider?.firstOrNull()?.providerInfo?.displayName)
        }

        fun decodeOrganizationIdArgument(arguments: Bundle?): String {
            return arguments?.getString(organizationIdArg).orEmpty()
        }

        fun decodeOrganizationNameArgument(arguments: Bundle?): String {
            return Uri.decode(arguments?.getString(organizationNameArg).orEmpty())
        }

        fun decodeSourceArgument(arguments: Bundle?): ConfigSource {
            val sourceString = arguments?.getString(sourceArg).orEmpty()
            return try {
                ConfigSource.valueOf(sourceString)
            } catch (ex: Exception) {
                ConfigSource.Unknown
            }
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