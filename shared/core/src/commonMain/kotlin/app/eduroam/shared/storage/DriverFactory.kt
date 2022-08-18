package app.eduroam.shared.storage

import com.squareup.sqldelight.db.SqlDriver

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): EduRoamDatabase {
    val driver = driverFactory.createDriver()
    val database = EduRoamDatabase(driver)
    // Do more work with the database (see below).
    return database
}