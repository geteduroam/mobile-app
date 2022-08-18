package app.eduroam.shared.storage

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(EduRoamDatabase.Schema, "eduroam.db")
}