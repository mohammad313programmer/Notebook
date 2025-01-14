package ir.NotesAppByRoom.notes.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.NotesAppByRoom.notes.db.DBHandler

@Entity(tableName = DBHandler.NOTES_TABLE)
data class NotesEntity(

    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo var title: String = "",
    @ColumnInfo var detail: String = "",
    @ColumnInfo var deleteState: Boolean = false,
    @ColumnInfo var date: String = "",

    @ColumnInfo var isPinned: Boolean = false

)
