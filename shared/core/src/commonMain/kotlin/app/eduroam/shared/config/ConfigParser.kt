package app.eduroam.shared.config

/**
 * Public contract that parses the eap byte array into a shareable object structure.
 * EAP xml definition: https://github.com/GEANT/CAT/blob/master/devices/xml/eap-metadata.xsd#L161-L170
 * */
interface ConfigParser {
    suspend fun parse(source: ByteArray): WifiConfigData
}