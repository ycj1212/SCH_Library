package com.example.sch_library

<<<<<<< HEAD
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        viewManager = LinearLayoutManager(context)
        viewAdapter = ViewAdapter()
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
=======
import androidx.fragment.app.Fragment

class BasketFragment : Fragment() {
>>>>>>> 72ce08b77bda28f29da7a4371941cf529d43e961
}