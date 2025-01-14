package ir.NotesAppByRoom.notes.ui


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.NotesAppByRoom.notes.R
import ir.NotesAppByRoom.notes.adapter.SearchingAdapter
import ir.NotesAppByRoom.notes.databinding.FragmentSearchingBinding
import ir.NotesAppByRoom.notes.db.DBHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchingFragment : Fragment() {

    private lateinit var binding: FragmentSearchingBinding
    private lateinit var adapter: SearchingAdapter
    private lateinit var db: DBHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSearchingBinding.inflate(inflater)

        return binding.root

    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerNotesInSearching.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.recyclerNotesInSearching.setHasFixedSize(true)

        db = DBHandler.getDatabase(requireContext())

        lifecycleScope.launch {

            withContext(Dispatchers.IO) {

                val allNotes = db.notesDao().getAllNotesForRecyclerSearching()

                withContext(Dispatchers.Main) {

                    allNotes.collect {

                        adapter = SearchingAdapter(requireContext(), ArrayList(it), db.notesDao())

                        binding.recyclerNotesInSearching.adapter = adapter

                        if (adapter.allData.size > 0) {

                            binding.lottieAnimationView.visibility = View.GONE
                        }

                        binding.textView.text = "تعداد یادداشت ها : ${it.size}"

                    }


                }

            }

        }

        binding.searchViewMain.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                adapter.filter.filter(newText)

                return false
            }

        })

        binding.imgBack.setOnClickListener {

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.containerView, MainFragment())
                .commit()

        }

        binding.imgHelp.setOnClickListener {

            if (binding.txtHelp.isVisible) {

                binding.txtHelp.visibility = View.GONE

            } else {

                binding.txtHelp.visibility = View.VISIBLE
            }

        }

    }

}