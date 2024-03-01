package app.eduroam.geteduroam.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.ui.util.fastJoinToString
import androidx.core.database.sqlite.transaction
import app.eduroam.geteduroam.models.Organization


private const val DATABASE_NAME = "organizations"
private const val DATABASE_VERSION = 1
class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Create db tables
        db?.execSQL("CREATE VIRTUAL TABLE organization USING fts4(" +
                "rowIndex," +
                "abbreviation," +
                "name," +
                "tokenize=unicode61);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // This is the first version, no upgrade to do yet
    }
    fun loadIntoDatabase(organizations: List<Organization>) {
        val database = writableDatabase
        database.transaction {
            delete("organization",  null, null)
            val values = ContentValues()
            organizations.forEachIndexed { index, organization ->
                values.clear()
                val name = organization.name ?: organization.id
                val abbreviation = name.split(",", " ","'", "\"", "(", ")", "-", ".").joinToString("") { it.take(1) }
                values.put("rowIndex", index)
                values.put("abbreviation", abbreviation)
                values.put("name", name)
                insert("organization", null, values)
            }
        }
    }

    fun getIndicesForFilter(filter: String): List<Int> {
        val database = readableDatabase

        val cursor: Cursor = database.rawQuery("SELECT rowIndex FROM organization WHERE organization MATCH ?", arrayOf("*$filter*"))
        val indices = mutableListOf<Int>()
        while (cursor.moveToNext()) {
            indices.add(cursor.getInt(0))
        }

        cursor.close()
        return indices
    }
}