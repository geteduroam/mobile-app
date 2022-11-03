package app.eduroam.geteduroam.wifi.configurator.exception

/**
 * Exception indicating a value in the provided configuration is missing or doesn't match a constraint
 */
class EapConfigValueException : EapConfigException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
}