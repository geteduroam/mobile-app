package app.eduroam.shared.ktor

import app.eduroam.shared.response.InstitutionResult
import co.touchlab.stately.ensureNeverFrozen
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

class InstitutionApiImpl(private val log: KermitLogger, engine: HttpClientEngine) : InstitutionApi {

    private val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(json = Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = true
                allowSpecialFloatingPointValues = true
                allowStructuredMapKeys = true
                prettyPrint = false
                useArrayPolymorphism = false
            })
        }
        install(Logging) {
            logger = object : KtorLogger {
                override fun log(message: String) {
                    log.v { message }
                }
            }

            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            val timeout = 30000L
            connectTimeoutMillis = timeout
            requestTimeoutMillis = timeout
            socketTimeoutMillis = timeout
        }
    }

    init {
        ensureNeverFrozen()
    }

    override suspend fun getJsonFromApi(): InstitutionResult {
        log.d { "Fetching institutions list from network" }
        return client.get {
            institutions("v1/discovery.json")
        }.body()
    }

    suspend fun test(downloadUrl: String) {
    }

    override suspend fun downloadEapFile(eapConfigEndpoint: String): ByteArray {
        log.d("Download EAP file")
        val response = client.get(eapConfigEndpoint) {
            onDownload { bytesSentTotal, contentLength ->
                log.d("Received $bytesSentTotal bytes from $contentLength")
            }
        }
        return response.body()
    }

    private fun HttpRequestBuilder.institutions(path: String) {
        url {
            takeFrom("https://discovery.eduroam.app/")
            encodedPath = path
        }
    }
}
