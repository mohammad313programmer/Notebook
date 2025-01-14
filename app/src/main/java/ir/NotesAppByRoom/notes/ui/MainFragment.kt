package ir.NotesAppByRoom.notes.ui


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.NotesAppByRoom.notes.R
import ir.NotesAppByRoom.notes.adapter.NotesAdapter
import ir.NotesAppByRoom.notes.databinding.FragmentMainBinding
import ir.NotesAppByRoom.notes.db.DBHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.*

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: NotesAdapter
    private lateinit var db: DBHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.inflate(inflater)
        initRecycler()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddNotes.setOnClickListener {
            val intent = Intent(requireContext(), AddNotesActivity::class.java)
            intent.putExtra("newNotes", true)
            startActivity(intent)
        }

        binding.imgRecycleBin.setOnClickListener {

            fragmentShow(RecycleBinFragment())

        }

        binding.searchView.setOnClickListener {

            fragmentShow(SearchingFragment())

        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.ACTION_STATE_DRAG or ItemTouchHelper.ACTION_STATE_SWIPE,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                // تعویض موقعیت آیتم‌ها در منبع داده
                Collections.swap(adapter.allData, fromPosition, toPosition)

                // اطلاع به آداپتر برای به‌روزرسانی RecyclerView
                adapter.notifyItemMoved(fromPosition, toPosition)

                return true

            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                adapter.changeDeleteState(adapter.allData[viewHolder.adapterPosition])

            }


        }).attachToRecyclerView(binding.recyclerNotes)

    }

    private fun initRecycler() {

        db = DBHandler.getDatabase(requireContext())

        binding.recyclerNotes.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        //این خط به ریسایکلر ویو اطلاع می‌دهد که اندازه آیتم‌ها ثابت است و تغییر نمی‌کند
        // این باعث بهبود عملکرد می‌شود، زیرا نیازی نیست که ریسایکلر ویو هر بار اندازه آیتم‌ها را دوباره محاسبه کند
        binding.recyclerNotes.setHasFixedSize(true)

        adapter = NotesAdapter(requireContext(), arrayListOf(), db.notesDao())

        binding.recyclerNotes.adapter = adapter

        lifecycleScope.launch {

            db.notesDao().getNotesForRecycler(false)

                .flowOn(Dispatchers.IO)

                .collect { notes ->

                    //adapter.dataUpdate(ArrayList(notes))

                    adapter.updateList(ArrayList(notes))

                    if (adapter.allData.size > 0) {

                        binding.lottieAnimationView.visibility = View.GONE
                    }

                }

        }

    }

    private fun fragmentShow(fragment: Fragment) {

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.containerView, fragment)
            .commit()

    }

}