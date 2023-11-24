package app.eduroam.geteduroam.config

import app.eduroam.geteduroam.config.model.EAPIdentityProviderList
import app.eduroam.geteduroam.extensions.DateJsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.transform.InvalidFormatException
import org.simpleframework.xml.transform.RegistryMatcher
import org.simpleframework.xml.transform.Transform
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class AndroidConfigParser {

    class DateFormatTransformer(private val dateFormat: DateFormat) : Transform<Date?> {
        override fun read(value: String?): Date? {
            if (value == null) {
                return null
            }
            return dateFormat.parse(value)
        }

        override fun write(value: Date?): String {
            if (value == null) {
                return ""
            }
            return dateFormat.format(value)
        }

    }

    suspend fun parse(source: ByteArray): EAPIdentityProviderList = withContext(Dispatchers.IO) {
        val registryMatcher = RegistryMatcher()
        registryMatcher.bind(Date::class.java, DateFormatTransformer(DateJsonAdapter.SERVER_DATE_FORMAT))
        val persister = Persister(registryMatcher)
        persister.read(EAPIdentityProviderList::class.java, source.inputStream())
    }
}