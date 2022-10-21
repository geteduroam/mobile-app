package app.eduroam.shared.ktor

import app.eduroam.shared.response.InstitutionResult
import app.eduroam.shared.response.TokenResponse
import co.touchlab.stately.ensureNeverFrozen
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
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

            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            this.socketTimeoutMillis = 80_000L
            this.connectTimeoutMillis = 60_000L
            this.requestTimeoutMillis = 60_000L
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

    override suspend fun postToken(
        tokenUrl: String, code: String, redirectUri: String, clientId: String, codeVerifier: String
    ): TokenResponse {
        log.d { "Fetching token via POST on $tokenUrl" }
        return client.post {
            tokenEndpoint(tokenUrl)
            setBody(FormDataContent(Parameters.build {
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", redirectUri)
                append("client_id", clientId)
                append("code_verifier", codeVerifier)
            }))
        }.body()
    }

    override suspend fun downloadEapFile(
        eapConfigEndpoint: String, accessToken: String?
    ): ByteArray {
        log.d("Download EAP file")
        val response = if (accessToken == null) {
            client.get(eapConfigEndpoint) {
                onDownload { bytesSentTotal, contentLength ->
                    log.d("Received $bytesSentTotal bytes from $contentLength")
                }
            }
        } else {
            client.post(eapConfigEndpoint) {
                headers["Authorization"] = "Bearer $accessToken"
                onDownload { bytesSentTotal, contentLength ->
                    log.d("Received $bytesSentTotal bytes from $contentLength")
                }
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

    private fun HttpRequestBuilder.tokenEndpoint(tokenUrl: String) {
        url {
            takeFrom(tokenUrl)
        }
    }
}
