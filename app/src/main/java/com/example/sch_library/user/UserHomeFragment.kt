package com.example.sch_library.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.BookInfo
import com.example.sch_library.R
import com.example.sch_library.ViewAdapter

lateinit var viewAdapter: ViewAdapter

class UserHomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)

        val basketButton: Button = view.findViewById(R.id.button_basket)
        val orderButton: Button = view.findViewById(R.id.button_order)
        val selectedCount: TextView = view.findViewById(R.id.textview_selected_count)
        val userHomeButtonsLayout: LinearLayout = view.findViewById(R.id.layout_user_home_buttons)

        viewManager = LinearLayoutManager(context)
        viewAdapter = ViewAdapter(1)
        viewAdapter.addViewForUser(basketButton, orderButton, userHomeButtonsLayout, selectedCount)
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