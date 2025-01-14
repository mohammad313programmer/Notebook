package ir.NotesAppByRoom.notes.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ir.NotesAppByRoom.notes.R
import ir.NotesAppByRoom.notes.databinding.CustomDialogBinding
import ir.NotesAppByRoom.notes.databinding.ListItemNotesInSearchingBinding
import ir.NotesAppByRoom.notes.db.dao.NotesDAO
import ir.NotesAppByRoom.notes.db.model.NotesEntity
import ir.NotesAppByRoom.notes.ui.AddNotesActivity
import kotlinx.coroutines.*
import java.util.*

class SearchingAdapter(

    private val context: Context,
    var allData: ArrayList<NotesEntity>,
    private val dao: NotesDAO

) : RecyclerView.Adapter<SearchingAdapter.SearchingViewHolder>(), Filterable {

    private var allNotes = ArrayList<NotesEntity>()

    init {
        allNotes.addAll(allData)
    }

    inner class SearchingViewHolder(

        private val binding: ListItemNotesInSearchingBinding

    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun setData(data: NotesEntity) {

            binding.txtTitleNotes.text = data.title
            binding.txtDetailNotes.text = data.detail
            binding.txtDateNotes.text = data.date

            binding.root.setOnClickListener {

                val intent = Intent(context, AddNotesActivity::class.java)

                intent.putExtra("notesId", data.id)

                context.startActivity(intent)

            }

            binding.imgDeleteNotes.setOnClickListener {

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
                        date = data.date
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

                            snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.blue))

                            snackBar.show()

                            CoroutineScope(Dispatchers.Main).launch {
                                for (i in 6 downTo 1) { // حلقه شمارش معکوس

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

            binding.root.setOnLongClickListener {

                if (binding.imgDeleteNotes.isVisible && binding.imgShareNotes.isVisible) {

                    binding.imgDeleteNotes.visibility = View.GONE
                    binding.imgShareNotes.visibility = View.GONE

                } else {
                    binding.imgDeleteNotes.visibility = View.VISIBLE
                    binding.imgShareNotes.visibility = View.VISIBLE
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchingViewHolder =
        SearchingViewHolder(
            ListItemNotesInSearchingBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = allData.size

    override fun onBindViewHolder(holder: SearchingViewHolder, position: Int) {

        holder.setData(allData[position])

    }

    override fun getFilter(): Filter =

        object : Filter() {

            override fun performFiltering(text: CharSequence?): FilterResults {

                val filterResults = FilterResults()
                val searchQuery = text.toString().lowercase(Locale.ROOT) //?: ""
                val filteredList = ArrayList<NotesEntity>()

                if (searchQuery.isEmpty()) {

                    filteredList.addAll(allNotes)

                } else {

                    for (item in allNotes) {
                        if (item.title.lowercase(Locale.ROOT).contains(searchQuery) /*||
                            item.detail.lowercase(Locale.ROOT).contains(searchQuery)*/
                        ) {
                            filteredList.add(item)
                        }
                    }
                }

                filterResults.values = filteredList
                return filterResults

            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                val newList = results?.values as? ArrayList<NotesEntity> ?: ArrayList()
                val diffCallback = DiffUtil.calculateDiff(
                    RecyclerDiffUtils(allData, newList)
                )
                allData.clear()
                allData.addAll(newList)
                diffCallback.dispatchUpdatesTo(this@SearchingAdapter)

            }


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