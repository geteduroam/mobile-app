package app.eduroam.geteduroam.wifi.configurator.exception

/**
 * Exception indicating that the client certificate could not be parsed
 */
class EapConfigClientCertificateException(message: String?, cause: Throwable?) :
    EapConfigException(message, cause)