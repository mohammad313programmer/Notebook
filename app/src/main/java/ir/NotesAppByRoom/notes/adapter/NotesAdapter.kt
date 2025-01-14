package ir.NotesAppByRoom.notes.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.NotesAppByRoom.notes.R
import ir.NotesAppByRoom.notes.databinding.CustomDialogBinding
import ir.NotesAppByRoom.notes.databinding.ListItemNotesBinding
import ir.NotesAppByRoom.notes.db.dao.NotesDAO
import ir.NotesAppByRoom.notes.db.model.NotesEntity
import ir.NotesAppByRoom.notes.ui.AddNotesActivity
import kotlinx.coroutines.*

class NotesAdapter(

    private val context: Context,
    var allData: ArrayList<NotesEntity>,
    private val dao: NotesDAO

) : RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    inner class NotesViewHolder(

        private val binding: ListItemNotesBinding

    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(data: NotesEntity) {

            binding.txtTitleNotes.text = data.title
            binding.txtDetailNotes.text = data.detail
            binding.txtDateNotes.text = data.date

            if (data.isPinned) {

                binding.imgPinNotes.visibility = View.VISIBLE
                binding.imgPinNotes.setColorFilter(Color.RED)
                binding.imgPinNotes.isEnabled = false
                binding.imgPinNotes.alpha = 0.5f

            } else {

                binding.imgPinNotes.isEnabled = true
                binding.imgPinNotes.alpha = 1.0f

                binding.imgPinNotes.visibility = View.GONE
                binding.imgPinNotes.setColorFilter(Color.WHITE)

            }

            // بازنشانی ویزیبیلیتی برای دکمه‌ها
            binding.imgDeleteNotes.visibility = View.GONE
            binding.imgShareNotes.visibility = View.GONE
            binding.imgUnPinNotes.visibility = View.GONE
            if (!data.isPinned) {
                binding.imgPinNotes.visibility = View.GONE
            }

            binding.root.setOnClickListener {

                val intent = Intent(context, AddNotesActivity::class.java)

                intent.putExtra("notesId", data.id)

                context.startActivity(intent)

            }

            binding.imgDeleteNotes.setOnClickListener {

                showDeleteDialog(data, binding)

            }

            binding.imgPinNotes.setOnClickListener {

                pinNote(adapterPosition)

            }

            binding.imgUnPinNotes.setOnClickListener {

                pinNote(adapterPosition)

            }

            binding.root.setOnLongClickListener {

                if (
                    !binding.imgDeleteNotes.isVisible && !binding.imgShareNotes.isVisible ||
                    !binding.imgPinNotes.isVisible
                ) {

                    binding.imgDeleteNotes.visibility = View.VISIBLE
                    binding.imgShareNotes.visibility = View.VISIBLE

                    if (!data.isPinned) {
                        binding.imgPinNotes.visibility = View.VISIBLE
                        binding.imgUnPinNotes.visibility = View.GONE
                    } else {

                        binding.imgUnPinNotes.visibility = View.VISIBLE

                    }

                } else {

                    binding.imgDeleteNotes.visibility = View.GONE
                    binding.imgShareNotes.visibility = View.GONE
                    binding.imgUnPinNotes.visibility = View.GONE

                    if (!data.isPinned) {
                        binding.imgPinNotes.visibility = View.GONE
                    }

                }

                return@setOnLongClickListener true

            }

            binding.imgShareNotes.setOnClickListener {

                shareText(
                    "<<<عنوان>>> : ${data.title}\n " +
                            "<<<متن>>> : \n${data.detail}\n " +
                            "\n-------------------------------------\n***From Notes***"
                )

            }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun showDeleteDialog(data: NotesEntity, binding: ListItemNotesBinding) {

        val dialog = Dialog(context)
        val binding2 = CustomDialogBinding.inflate(LayoutInflater.from(context))
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding2.txtDialog.text = "آیا( ${data.title} ) رو بطور کامل حذف کنم؟"

        binding2.btnDelete.setOnClickListener {

            val deleteNote = NotesEntity(
                id = data.id,
                title = data.title,
                detail = data.detail,
                deleteState = data.deleteState,
                date = data.date,
                isPinned = data.isPinned
            )

            CoroutineScope(Dispatchers.IO).launch {

                dao.deleteNotes(deleteNote)

                withContext(Dispatchers.Main) {

                    val snackBar =
                        Snackbar.make(
                            binding.root,
                            "",
                            Snackbar.LENGTH_INDEFINITE
                        )

                    snackBar.setAction("بازگردانی") {

                        CoroutineScope(Dispatchers.IO).launch {

                            dao.saveNotes(deleteNote)

                            withContext(Dispatchers.Main) {

                                showText("بازگردانی شد.")

                            }

                        }

                    }

                    snackBar.setActionTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.blue
                        )
                    )

                    snackBar.show()

                    CoroutineScope(Dispatchers.Main).launch {
                        for (i in 6 downTo 1) {

                            snackBar.setText("یادداشت حذف شد...  $i  ثانیه")
                            delay(1000) // تاخیر یک ثانیه ای

                        }

                        snackBar.dismiss()

                    }

                }

            }

            dialog.dismiss()

        }

        binding2.btnBack.setOnClickListener {

            dialog.dismiss()

        }

        dialog.setContentView(binding2.root)
        dialog.show()

    }

    fun pinNote(position: Int) {
        val note = allData[position]
        val newPinnedState = !note.isPinned

        // آپدیت فوری UI
        note.isPinned = newPinnedState
        notifyItemChanged(position)

        CoroutineScope(Dispatchers.IO).launch {

            try {

                dao.editNotes(note.copy(isPinned = newPinnedState)) // آپدیت دیتابیس با استفاده از copy

                withContext(Dispatchers.Main) {

                    //showText("یادداشت ${if (newPinnedState) "سنجاق" else "از سنجاق برداشته"} شد.")
                }

            } catch (e: Exception) {

                // در صورت بروز خطا، وضعیت را به حالت قبل برگردانید و پیام خطا نمایش دهید
                withContext(Dispatchers.Main) {

                    note.isPinned = !newPinnedState

                    notifyItemChanged(position)

                    showText("خطا در ذخیره تغییرات.")

                    Log.e("PinNoteError", "Error updating pin status", e) // ثبت لاگ خطا برای دیباگ

                }
            }
        }
    }

    fun changeDeleteState(data: NotesEntity) {

        CoroutineScope(Dispatchers.IO).launch {

            dao.editNotes(
                NotesEntity(
                    id = data.id,
                    title = data.title,
                    detail = data.detail,
                    date = data.date,
                    deleteState = true
                )
            )

            withContext(Dispatchers.Main) {

                showText("به بایگانی منتقل شد")

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder =
        NotesViewHolder(
            ListItemNotesBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = allData.size

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {

        holder.setData(allData[position])

    }

    /* fun dataUpdate(newList: ArrayList<NotesEntity>) {

         val diffCallback = RecyclerDiffUtils(allData, newList)
         val diffResult = DiffUtil.calculateDiff(diffCallback)

         allData.clear()
         allData.addAll(newList)
         diffResult.dispatchUpdatesTo(this)

     }*/

    fun updateList(newList: ArrayList<NotesEntity>) {

        newList.sortWith { o1, o2 ->

            if (o1.isPinned && !o2.isPinned) {

                -1 // o1 باید قبل از o2 باشد

            } else if (!o1.isPinned && o2.isPinned) {

                1 // o2 باید قبل از o1 باشد

            } else {

                0 // ترتیب o1 و o2 تغییری نکند

            }

        }

        /*Collections.sort(newList, object : Comparator<NotesEntity> {

       override fun compare(o1: NotesEntity, o2: NotesEntity): Int {
           if (o1.isPinned && !o2.isPinned) {
               return -1 // o1 باید قبل از o2 باشد
           } else if (!o1.isPinned && o2.isPinned) {
               return 1 // o2 باید قبل از o1 باشد
           } else {
               return 0 // ترتیب o1 و o2 تغییری نکند
           }
       }

   })*/

        val diffCallback = RecyclerDiffUtils(allData, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        allData.clear()
        allData.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }


    // وظیفه این اینتنت به اشتراک گذاری متون میباشد
    private fun shareText(text: String) {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)

        // ممکن است هیچ نرم افزاری برای انجام کار مدنظر پیدا نشود
        // پس دلیل استفاده از ترای و کچ نیز همین است تا برنامه متوقف نشود
        try {

            context.startActivity(intent)

        } catch (e: ActivityNotFoundException) {

            showText("Activity Share Not Found")

        }

    }

    private fun showText(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

}