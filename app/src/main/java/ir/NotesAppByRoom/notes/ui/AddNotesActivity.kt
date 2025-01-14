package ir.NotesAppByRoom.notes.ui

import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ir.NotesAppByRoom.notes.R
import ir.NotesAppByRoom.notes.databinding.ActivityAddNotesBinding
import ir.NotesAppByRoom.notes.db.DBHandler
import ir.NotesAppByRoom.notes.db.model.NotesEntity
import ir.NotesAppByRoom.notes.utils.PersianDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNotesBinding
    private lateinit var note: NotesEntity
    private lateinit var db: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.Ferozeh200)

        val type = intent.getBooleanExtra("newNotes", false)
        val id = intent.getIntExtra("notesId", 0)

        db = DBHandler.getDatabase(this)
        val dao = db.notesDao()

        if (type) {

            binding.txtDate.text = getDate()

        } else {

            lifecycleScope.launch {

                withContext(Dispatchers.IO) {

                    note = dao.getNotesById(id)

                    withContext(Dispatchers.IO) {

                        val edit = Editable.Factory()

                        binding.edtTitleNotes.text = edit.newEditable(note.title)
                        binding.edtDetailNotes.text = edit.newEditable(note.detail)
                        binding.txtDate.text = note.date

                    }

                }

            }


        }

        //محدوده

        binding.imgSave.setOnClickListener {

            val title = binding.edtTitleNotes.text.toString().trim()
            val detail = binding.edtDetailNotes.text.toString().trim()

            if (title.isNotEmpty()) {

                lifecycleScope.launch {

                    withContext(Dispatchers.IO) {

                        if (type) {

                            dao.saveNotes(
                                NotesEntity(
                                    title = title,
                                    detail = detail,
                                    deleteState = false,
                                    date = getDate()
                                )
                            )

                        } else {

                            dao.editNotes(
                                NotesEntity(
                                    id = id,
                                    title = title,
                                    detail = detail,
                                    deleteState = false,
                                    date = getDate(),
                                    isPinned = note.isPinned
                                )


                            )

                        }

                        withContext(Dispatchers.Main) {

                            showText("ذخیره شد")

                            finish()

                        }

                    }

                }

            } else
                showText("عنوان  بگذارید")

        }

        //محدوده

        binding.imgBack.setOnClickListener { finish() }

    }

    private fun showText(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun getDate(): String {
        val persianDate = PersianDate()
        val currentDate = "${persianDate.year} / ${persianDate.month} / ${persianDate.day}"
        val currentTime = "${persianDate.hour} :${persianDate.min}"
        return "$currentDate  ||  $currentTime"
    }

}