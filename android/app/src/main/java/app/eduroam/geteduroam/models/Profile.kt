package app.eduroam.geteduroam.models

import android.os.Parcelable
import app.eduroam.geteduroam.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
@JsonClass(generateAdapter = true)
data class Profile(
    @Json(name = "eapconfig_endpoint")
    val eapconfigEndpoint: String? = null,
    val id: String,
    val name: Map<String, String>,
    val oauth: Boolean = false,
    @Json(name = "authorization_endpoint")
    val authorizationEndpoint: String? = null,
    @Json(name = "token_endpoint")
    val tokenEndpoint: String? = null,
    val redirect: String? = null,
    val type: Type,
    @Json(name = "letswifi_endpoint")
    val letswifiEndpoint: String? = null
) : Parcelable {

    @JsonClass(generateAdapter = false)
    enum class Type {
        @Json(name = "letswifi")
        letswifi,
        @Json(name = "eap-config")
        eapConfig,
        unknown
    }

    fun getLocalizedName(): String {
        val userLanguage = Locale.getDefault().language.lowercase()
        return name[userLanguage] ?: // 1st option: the name in the user's language
        name[LANGUAGE_KEY_FALLBACK] ?: // 2nd option: the name in the fallback language (english)
        name.values.firstOrNull() ?: // 3rd option: any name we can find
        id // 4th option: the ID, which is always set
    }
    fun createConfiguration() : Configuration {
        return Configuration(
            clientId = BuildConfig.OAUTH_CLIENT_ID,
            scope = "eap-metadata",
            redirect = BuildConfig.OAUTH_REDIRECT_URI,
            authEndpoint = authorizationEndpoint ?: "",
            tokenEndpoint = tokenEndpoint ?: "",
            discovery = BuildConfig.DISCOVERY_BASE_URL + "v1",
            isHttpsRequired = false
        )
    }

    companion object {
        private const val LANGUAGE_KEY_FALLBACK = "any"
    }
}

