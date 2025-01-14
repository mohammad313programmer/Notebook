package ir.NotesAppByRoom.notes.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.NotesAppByRoom.notes.db.dao.NotesDAO
import ir.NotesAppByRoom.notes.db.model.NotesEntity

@Database(
    entities = [NotesEntity::class],
    version = DBHandler.DATABASE_VERSION
)
abstract class DBHandler : RoomDatabase() {

    abstract fun notesDao(): NotesDAO

    companion object {
        private const val DATABASE_NAME = "notes_database"
        const val DATABASE_VERSION = 2
        const val NOTES_TABLE = "notesTable"

        private var instance: DBHandler? = null

        // تعریف Migration برای افزودن ستون isPinned
         private val MIGRATION_1_2 = object : Migration(1, 2) {
             override fun migrate(db: SupportSQLiteDatabase) {
                 // اضافه کردن ستون isPinned به جدول
                 db.execSQL(
                     "ALTER TABLE $NOTES_TABLE ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0"
                 )
             }
         }

        fun getDatabase(context: Context): DBHandler {

            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    DBHandler::class.java,
                    DATABASE_NAME
                )
                    //.fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }

            return instance!!

        }

        /* fun getDatabase(context: Context): DBHandler {

             val instance by lazy {
                 Room.databaseBuilder(
                     context.applicationContext,
                     DBHandler::class.java,
                     DATABASE_NAME
                 )
                     .fallbackToDestructiveMigration()
                     .build()
             }
             return instance
         }*/

    }
}