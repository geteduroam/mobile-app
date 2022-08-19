package app.eduroam.geteduroam.wifi.configurator.exception

/**
 * Exception indicating a problem with one or more CA certificates or the chain
 */
class EapConfigCAException(message: String?, cause: Throwable?) : EapConfigException(message, cause)