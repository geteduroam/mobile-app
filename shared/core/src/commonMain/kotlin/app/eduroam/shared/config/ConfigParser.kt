package app.eduroam.shared.config

interface ConfigParser {
    suspend fun parse(source: ByteArray): WifiConfigData
}