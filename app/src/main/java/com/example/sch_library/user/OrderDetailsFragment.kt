package com.example.sch_library.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.R

class OrderDetailsFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var orderViewAdapter: OrderDetailsViewAdapter
    lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_order_details, container, false)

        viewManager = LinearLayoutManager(context)
        orderViewAdapter = OrderDetailsViewAdapter()
        // 주문내역 추가

        recyclerView = view.findViewById(R.id.recyclerview_order_details)
        recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = orderViewAdapter
        }

        return view
    }
}

class OrderInfo(var orderNumber: String, var orderDate: String, var orderTotalPrice: Int, var orderStatus: String)

class OrderDetailsViewAdapter : RecyclerView.Adapter<OrderDetailsViewAdapter.ViewHolder>() {
    private val items = ArrayList<OrderInfo>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderNumber: TextView = itemView.findViewById(R.id.textview_order_number)
        private val orderDate: TextView = itemView.findViewById(R.id.textview_order_date)
        private val orderTotalPrice: TextView = itemView.findViewById(R.id.textview_order_total_price)
        private val orderStatus: TextView = itemView.findViewById(R.id.textview_order_status)

        fun setItem(item: OrderInfo) {
            orderNumber.text = item.orderNumber
            orderDate.text = item.orderDate
            orderTotalPrice.text = item.orderTotalPrice.toString()
            orderStatus.text = item.orderStatus
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.info_order, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    override fun getItemCount(): Int = items.size
}