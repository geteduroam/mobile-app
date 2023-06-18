package app.eduroam.geteduroam.di.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.eduroam.geteduroam.models.Configuration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import timber.log.Timber
import java.io.IOException

class StorageRepository(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(
        name = "oauth_settings"
    )

    val authState: Flow<AuthState?> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Error reading preferences.")
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        val authState = preferences[PreferencesKeys.CURRENT_AUTHSTATE]
        authState?.let {
            AuthState.jsonDeserialize(it)
        }
    }

    val isAuthorized: Flow<Boolean> =
        authState.map { it != null && it.isAuthorized && !it.needsTokenRefresh }

    val authRequest: Flow<AuthorizationRequest?> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Error reading preferences.")
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        val authRequest = preferences[PreferencesKeys.CURRENT_AUTHREQUEST]
        authRequest?.let {
            AuthorizationRequest.jsonDeserialize(it)
        }
    }

    val clientId: Flow<String?> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Error reading preferences.")
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[PreferencesKeys.CLIENT_ID]
    }

    val lastKnownConfigHash: Flow<Int?> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Error reading preferences.")
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[PreferencesKeys.LAST_KNOWN_CONFIG_HASH]
    }

    suspend fun clearInvalidAuth() = context.dataStore.edit { settings ->
        settings.remove(PreferencesKeys.CURRENT_AUTHREQUEST)
        settings.remove(PreferencesKeys.CURRENT_AUTHSTATE)
    }

    suspend fun clearAll() = context.dataStore.edit { settings ->
        settings.clear()
    }

    suspend fun saveCurrentAuthState(authState: AuthState?) = context.dataStore.edit { settings ->
        settings[PreferencesKeys.CURRENT_AUTHSTATE] =
            authState?.jsonSerializeString() ?: settings.remove(PreferencesKeys.CURRENT_AUTHSTATE)
    }

    suspend fun saveCurrentAuthRequest(authRequest: AuthorizationRequest?) =
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.CURRENT_AUTHREQUEST] = authRequest?.jsonSerializeString()
                ?: settings.remove(PreferencesKeys.CURRENT_AUTHREQUEST)
        }

    suspend fun saveClientId(clientId: String) = context.dataStore.edit { settings ->
        settings[PreferencesKeys.CLIENT_ID] = clientId
    }

    suspend fun acceptNewConfiguration(configHash: Int) = context.dataStore.edit { settings ->
        settings[PreferencesKeys.LAST_KNOWN_CONFIG_HASH] = configHash
    }

    suspend fun isAuthenticatedForConfig(config: Configuration): Boolean {
        val lastKnownHash = lastKnownConfigHash.first()
        return config.hashCode() == lastKnownHash && isAuthorized.first()
    }

    private object PreferencesKeys {
        val CURRENT_AUTHSTATE = stringPreferencesKey("current_authstate")
        val CURRENT_AUTHREQUEST = stringPreferencesKey("current_authrequest")
        val CLIENT_ID = stringPreferencesKey("currentClientId")
        val LAST_KNOWN_CONFIG_HASH = intPreferencesKey("last_known_configuration_hash")
    }
}