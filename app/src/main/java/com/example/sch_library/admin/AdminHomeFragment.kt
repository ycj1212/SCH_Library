package com.example.sch_library.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.BookInfo
import com.example.sch_library.R
import com.example.sch_library.ViewAdapter

lateinit var viewAdapter: ViewAdapter

class AdminHomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        val addButton: Button = view.findViewById(R.id.button_add_book)
        val deleteButton: Button = view.findViewById(R.id.button_delete_selected)

        viewManager = LinearLayoutManager(context)
        viewAdapter = ViewAdapter(0)
        viewAdapter.addViewForAdmin(addButton, deleteButton)
        for (i in 1 until 50) {
            viewAdapter.addItem(BookInfo(i, "책$i", i, i))
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_booklist).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        return view
    }
}