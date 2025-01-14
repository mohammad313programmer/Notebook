package ir.NotesAppByRoom.notes.db.dao

import androidx.room.*
import ir.NotesAppByRoom.notes.db.DBHandler
import ir.NotesAppByRoom.notes.db.model.NotesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDAO {

    @Insert
    fun saveNotes(vararg notes: NotesEntity)

    // فانکشن مخصوص انتقال یادداشت به سطل زباله (تغییر استیت یادداشت )
    // فانکشن مخصوص ادیت و ویرایش کامل یادداشت مورد نظر
    @Update
    fun editNotes(vararg notes: NotesEntity): Int

    //نمایش یادداشت ها برای ریسایکلرویو
    @Query("SELECT * FROM ${DBHandler.NOTES_TABLE} WHERE deleteState = :value")
    fun getNotesForRecycler(value: Boolean): Flow<List<NotesEntity>>

    //سلکت کردن تمام آیتم های ذخیره شده برای سرچ کردن در آن ها
    @Query("SELECT * FROM ${DBHandler.NOTES_TABLE}")
    fun getAllNotesForRecyclerSearching(): Flow<List<NotesEntity>>

    //نمایش یادداشت ها برای وقتی که روی هر آیتم کلیک میکنیم
    @Query("SELECT * FROM ${DBHandler.NOTES_TABLE} WHERE id = :id")
    fun getNotesById(id: Int): NotesEntity

    @Delete
    fun deleteNotes(vararg notes: NotesEntity): Int

}