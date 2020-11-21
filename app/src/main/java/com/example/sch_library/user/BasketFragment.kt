package com.example.sch_library.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.BookInfo
import com.example.sch_library.R
import com.example.sch_library.ViewAdapter

class BasketFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_basket, container, false)

        val orderButton: Button = view.findViewById(R.id.button_order_basket)
        val deleteButton: Button = view.findViewById(R.id.button_delete_basket)
        val totalPrice: TextView = view.findViewById(R.id.textview_total_price)
        val checkAll: CheckBox = view.findViewById(R.id.checkbox_all)

        viewManager = LinearLayoutManager(context)
        viewAdapter = ViewAdapter(2)
        viewAdapter.addViewForBasket(orderButton, deleteButton, totalPrice, checkAll, view.context)
        for (i in 50 downTo 1) {
            viewAdapter.addItem(BookInfo(i, "책$i", i, i))
        }

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_basket).apply {
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