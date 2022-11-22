package app.eduroam.shared.config

class IOSConfigParser : ConfigParser {

    override suspend fun parse(source: ByteArray): WifiConfigData = WifiConfigData(ssids = listOf("Hello"), oids = listOf(1))

}