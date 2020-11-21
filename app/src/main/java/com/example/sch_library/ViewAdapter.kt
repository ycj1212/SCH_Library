package com.example.sch_library

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class ViewAdapter(classification: Int) : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {
    private val classification = classification // 0: 관리자, 1: 사용자, 2: 장바구니
    private val items = ArrayList<BookInfo>()
    private var count = 0

    private lateinit var addButton: Button
    private lateinit var deleteButton: Button

    private lateinit var basketButton: Button
    private lateinit var orderButton: Button
    private lateinit var userHomeButtonsLayout: LinearLayout
    private lateinit var selectedCount: TextView

    private lateinit var totalPrice: TextView
    private lateinit var checkAll: CheckBox

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var bookNumber: TextView = itemView.findViewById(R.id.booknumber)
        private var bookTitle: TextView = itemView.findViewById(R.id.booktitle)
        private var bookStock: TextView = itemView.findViewById(R.id.bookstock)
        private var bookPrice: TextView = itemView.findViewById(R.id.bookprice)

        var checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        private var bookCount: TextView = itemView.findViewById(R.id.textview_count)
        private var decreaseCountButton: ImageButton = itemView.findViewById(R.id.button_decrease_count)
        private var increaseCountButton: ImageButton = itemView.findViewById(R.id.button_increase_count)

        init {
            itemView.setOnClickListener {
                when (classification) {
                    0 -> {  // 관리자
                        val dialog = BookUpdateDialog(itemView.context, items[adapterPosition])

                        dialog.show()
                    }
                    1 -> {   // 사용자
                        val dialog = BookInfoDialog(itemView.context, items[adapterPosition])
                        dialog.setDialogListener(object : BookInfoDialogListener {
                            override fun onClicked() {
                                notifyDataSetChanged()
                            }
                        })
                        dialog.show()
                    }
                    else -> {
                        // 장바구니
                        // 주문 / 삭제
                    }
                }
            }

            decreaseCountButton.setOnClickListener {
                if (items[adapterPosition].count > 1) {
                    items[adapterPosition].count--
                    bookCount.text = items[adapterPosition].count.toString()
                    if (classification == 2) {
                        totalPrice.text = getTotalPrice()
                    }
                }
            }

            increaseCountButton.setOnClickListener {
                if (items[adapterPosition].count < items[adapterPosition].stock) {
                    items[adapterPosition].count++
                    bookCount.text = items[adapterPosition].count.toString()
                    if (classification == 2) {
                        totalPrice.text = getTotalPrice()
                    }
                }
            }

            if (classification == 2) {
                checkBox.isChecked = true
            }
        }

        fun setItem(item: BookInfo) {
            bookNumber.text = item.number.toString()
            bookTitle.text = item.title
            bookStock.text = item.stock.toString()
            bookPrice.text = item.price.toString()
            bookCount.text = item.count.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.info_book, parent, false)

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

            if (classification == 0) {  // 관리자
                if (count == 0) {
                    addButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.GONE
                } else {
                    addButton.visibility = View.GONE
                    deleteButton.text = "${count}개 삭제"
                    deleteButton.visibility = View.VISIBLE
                }
            } else if (classification == 1) {   // 사용자
                if (count == 0) {
                    userHomeButtonsLayout.visibility = View.GONE
                } else {
                    userHomeButtonsLayout.visibility = View.VISIBLE
                    selectedCount.text = "${count}개 선택됨"
                }
            } else {    // 장바구니
                totalPrice.text = getTotalPrice()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getTotalPrice(): String {
        var sum = 0
        for (item in items) {
            if (item.isSelected) {
                sum += item.price * item.count
            }
        }
        return "총 $sum 원"
    }

    fun addItem(item: BookInfo) {
        items.add(item)
    }

    fun addViewForAdmin(addButton: Button, deleteButton: Button) {
        this.addButton = addButton
        this.deleteButton = deleteButton
    }

    fun addViewForUser(basketButton: Button, orderButton: Button, layout: LinearLayout, textView: TextView) {
        this.basketButton = basketButton
        this.orderButton = orderButton
        this.userHomeButtonsLayout = layout
        this.selectedCount = textView
    }

    fun addViewForBasket(orderButton: Button, deleteButton: Button, totalPrice: TextView, checkAll: CheckBox, context: Context) {
        this.orderButton = orderButton
        this.deleteButton = deleteButton
        this.totalPrice = totalPrice
        this.checkAll = checkAll

        checkAll.setOnClickListener {
            if (checkAll.isChecked) {
                count = items.size
                for (item in items) {
                    item.isSelected = true
                }
            } else {
                clear()
            }
            totalPrice.text = getTotalPrice()
            notifyDataSetChanged()
        }
    }

    fun getSelectedCount(): Int = count

    fun clear() {
        count = 0
        for (item in items) {
            item.isSelected = false
        }

        when (classification) {
            0 -> {
                addButton.visibility = View.VISIBLE
                deleteButton.visibility = View.GONE
            }
            1 -> {
                userHomeButtonsLayout.visibility = View.GONE
            }
            else -> {

            }
        }
    }
}

interface BookInfoDialogListener {
    fun onClicked()
}

class BookInfoDialog(context: Context, private val bookInfo: BookInfo) : Dialog(context) {
    lateinit var title: TextView
    lateinit var number: TextView
    lateinit var stock: TextView
    lateinit var price: TextView

    lateinit var count: TextView
    lateinit var decreaseCount: ImageButton
    lateinit var increaseCount: ImageButton

    lateinit var bookInfoDialogListener: BookInfoDialogListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_book_info)

        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params

        title = findViewById(R.id.textview_book_title)
        number = findViewById(R.id.textview_book_number)
        stock = findViewById(R.id.textview_book_stock)
        price = findViewById(R.id.textview_book_price)
        count = findViewById(R.id.textview_count)
        decreaseCount = findViewById(R.id.button_decrease_count)
        increaseCount = findViewById(R.id.button_increase_count)

        title.text = bookInfo.title
        number.text = bookInfo.number.toString()
        stock.text = bookInfo.stock.toString()
        price.text = bookInfo.price.toString()
        count.text = bookInfo.count.toString()

        decreaseCount.setOnClickListener {
            if (bookInfo.count > 1) {
                bookInfo.count--
                count.text = bookInfo.count.toString()
            }
        }

        increaseCount.setOnClickListener {
            if (bookInfo.count < bookInfo.stock) {
                bookInfo.count++
                count.text = bookInfo.count.toString()
            }
        }

        val basketButton: Button = findViewById(R.id.button_basket)
        basketButton.setOnClickListener {

        }

        val orderButton: Button = findViewById(R.id.button_order)
        orderButton.setOnClickListener {

        }
    }

    override fun onBackPressed() {
        bookInfoDialogListener.onClicked()
        dismiss()
    }

    fun setDialogListener(listener: BookInfoDialogListener) {
        bookInfoDialogListener = listener
    }
}

class BookUpdateDialog(context: Context, private val bookInfo: BookInfo) : Dialog(context) {
    lateinit var title: EditText
    lateinit var number: EditText
    lateinit var stock: EditText
    lateinit var price: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update_book)

        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params

        title = findViewById(R.id.edittext_book_title)
        number = findViewById(R.id.edittext_book_number)
        stock = findViewById(R.id.edittext_book_stock)
        price = findViewById(R.id.edittext_book_price)

        title.setText(bookInfo.title)
        number.setText(bookInfo.number)
        stock.setText(bookInfo.stock)
        price.setText(bookInfo.price)

        val updateButton: Button = findViewById(R.id.button_update_book)
        updateButton.setOnClickListener {

        }
    }

    override fun onBackPressed() {
        dismiss()
    }
}