package com.example.sch_library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewAdapter : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {
    private val items = ArrayList<BookInfo>()
    private var count = 0
    private lateinit var addButton: Button
    private lateinit var deleteButton: Button

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var bookNumber: TextView = itemView.findViewById(R.id.booknumber)
        private var bookTitle: TextView = itemView.findViewById(R.id.booktitle)
        private var bookStock: TextView = itemView.findViewById(R.id.bookstock)
        private var bookPrice: TextView = itemView.findViewById(R.id.bookprice)

        var checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        fun setItem(item: BookInfo) {
            bookNumber.text = item.number.toString()
            bookTitle.text = item.title
            bookStock.text = item.stock.toString()
            bookPrice.text = item.price.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.book_info, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
        holder.checkBox.isChecked = item.isSelected
        holder.checkBox.setOnClickListener {
            count = 0
            item.isSelected = holder.checkBox.isChecked

            for (item in items) {
                if (item.isSelected) {
                    count++
                }
            }

            if (count == 0) {
                addButton.visibility = View.VISIBLE
                deleteButton.visibility = View.GONE
            } else {
                addButton.visibility = View.GONE
                deleteButton.text = "${count}개 삭제"
                deleteButton.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: BookInfo) {
        items.add(item)
    }

    fun addButtonForAdmin(addButton: Button, deleteButton: Button) {
        this.addButton = addButton
        this.deleteButton = deleteButton
        deleteButton.setOnClickListener {
            println("성공!")
        }
    }

    fun getSelectedCount(): Int = count

    fun clear() {
        count = 0
        for (item in items) {
            item.isSelected = false
        }
        addButton.visibility = View.VISIBLE
        deleteButton.visibility = View.GONE
    }
}