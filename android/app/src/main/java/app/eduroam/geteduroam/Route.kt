package app.eduroam.geteduroam

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import app.eduroam.geteduroam.config.AndroidConfigParser
import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.models.Configuration
import app.eduroam.geteduroam.status.ConfigSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.net.URLDecoder
import java.net.URLEncoder

object NavTypes {
    val ConfigurationNavType = object: NavType<Configuration>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): Configuration? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(key, Configuration::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable(key)
            }
        }

        override fun parseValue(value: String): Configuration {
            val decoded = URLDecoder.decode(value, Charsets.UTF_8.name())
            return Json.decodeFromString(decoded)
        }

        override fun serializeAsValue(value: Configuration): String {
            val string = Json.encodeToString(value)
            return URLEncoder.encode(string, Charsets.UTF_8.name())
        }

        override fun put(bundle: Bundle, key: String, value: Configuration) {
            bundle.putParcelable(key, value)
        }
    }
    val ConfigSourceNavType = object: NavType<ConfigSource>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): ConfigSource? {
            val string = bundle.getString(key)
            return if (string.isNullOrEmpty()) {
                null
            } else {
                ConfigSource.valueOf(string)
            }
        }

        override fun parseValue(value: String): ConfigSource {
            return Json.decodeFromString(value)
        }

        override fun serializeAsValue(value: ConfigSource): String {
            return Json.encodeToString(value)
        }

        override fun put(bundle: Bundle, key: String, value: ConfigSource) {
            bundle.putString(key, value.name)
        }
    }
    val EAPIdentityProviderListNavType = object: NavType<EAPIdentityProviderList>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): EAPIdentityProviderList? {
            val string = bundle.getString(key)
            return if (string.isNullOrEmpty()) {
                null
            } else {
                val decodedString = URLDecoder.decode(string, Charsets.UTF_8.name())
                Json.decodeFromString(decodedString)
            }
        }

        override fun parseValue(value: String): EAPIdentityProviderList {
            val decoded = URLDecoder.decode(value, Charsets.UTF_8.name())
            return Json.decodeFromString(decoded)
        }

        override fun serializeAsValue(value: EAPIdentityProviderList): String {
            val string = Json.encodeToString(value)
            return URLEncoder.encode(string, Charsets.UTF_8.name())

        }

        override fun put(bundle: Bundle, key: String, value: EAPIdentityProviderList) {
            val string = Json.encodeToString(value)
            val encodedString = URLEncoder.encode(string, Charsets.UTF_8.name())
            bundle.putString(key, encodedString)
        }
    }
}

sealed class Route {
    @Serializable
    data object SelectInstitution : Route()
    @Serializable
    data object StatusScreen : Route()
    @Serializable
    @Parcelize
    data class SelectProfile(val institutionId: String?, val customHostUri: String?) : Route(), Parcelable
    @Serializable
    data class OAuth(val configuration: Configuration, val redirectUri: String?): Route()
    @Serializable
    data class WebViewFallback(val configuration: Configuration, val urlToLoad: String) : Route()
    @Serializable
    data class ConfigureWifi(
        val source: ConfigSource,
        val organizationId: String,
        val organizationName: String?,
        val eapIdentityProviderList: EAPIdentityProviderList
    ): Route() {
        companion object {
            suspend fun buildDeepLink(context: Context, fileUri: Uri): ConfigureWifi? = withContext(Dispatchers.IO) {
                // Read the contents of the file as XML
                val inputStream = context.contentResolver.openInputStream(fileUri) ?: return@withContext null
                val bytes = inputStream.readBytes()
                val configParser = AndroidConfigParser()
                return@withContext try {
                    val provider = configParser.parse(bytes)
                    inputStream.close()
                    ConfigureWifi(ConfigSource.File, "", null, provider)
                } catch (ex: Exception) {
                    Timber.w(ex, "Could not parse file opened!")
                    null
                }
            }
        }
    }
}