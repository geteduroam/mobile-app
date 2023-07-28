package app.eduroam.geteduroam.config

import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.simpleframework.xml.core.Persister


class AndroidConfigParser {
    suspend fun parse(source: ByteArray): EAPIdentityProviderList = withContext(Dispatchers.IO) {
        Persister().read(EAPIdentityProviderList::class.java, source.inputStream())
    }
}