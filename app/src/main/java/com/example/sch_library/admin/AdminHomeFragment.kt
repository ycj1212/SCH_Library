package com.example.sch_library.admin

import android.app.Dialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.BookInfo
import com.example.sch_library.IP_ADDRESS
import com.example.sch_library.R
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

lateinit var viewAdapter: ViewAdapter

private const val TAG = "AdminHome"

class AdminHomeFragment : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        searchView = view.findViewById(R.id.searchview)

        val addButton: Button = view.findViewById(R.id.button_add_book)
        val deleteButton: Button = view.findViewById(R.id.button_delete_selected)

        viewManager = LinearLayoutManager(context)
        viewAdapter = ViewAdapter(0)
        viewAdapter.addViewForAdmin(addButton, deleteButton)

        val task = UpdateBookList(context!!)
        task.execute("http://$IP_ADDRESS/booklist.php", "select * from 도서")

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

    class UpdateBookList(private val context: Context) : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val query = p0[1]

            val serverURL = p0[0]
            val postParameters = "query=$query"

            try {
                val url = URL(serverURL)
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.apply {
                    readTimeout = 5000
                    connectTimeout = 5000
                    requestMethod = "POST"
                    connect()
                }

                val outputStream = httpURLConnection.outputStream
                outputStream.apply {
                    write(postParameters.toByteArray())
                    flush()
                    close()
                }

                val responseStatusCode = httpURLConnection.responseCode
                Log.d(TAG, "POST response code - $responseStatusCode")

                val inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.inputStream
                } else {
                    httpURLConnection.errorStream
                }

                val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val sb = StringBuilder()
                var line: String? = null

                line = bufferedReader.readLine()
                while (line != null) {
                    sb.append(line)
                    line = bufferedReader.readLine()
                }
                bufferedReader.close()

                return sb.toString()
            } catch (e: Exception) {
                Log.d(TAG, "ConnectAccount: Error ", e)
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                val jsonObject = JSONObject(result)
                val jsonArray = jsonObject.getJSONArray("booklist")
                viewAdapter.cleanItems()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i);
                    val bookNumber = item.getString("booknumber")
                    val bookTitle = item.getString("booktitle")
                    val stock = item.getString("stock")
                    val price = item.getString("price")

                    viewAdapter.addItem(BookInfo(bookNumber.toInt(), bookTitle, stock.toInt(), price.toInt()))
                }

                viewAdapter.notifyDataSetChanged()
            } catch (e: JSONException) {
                Log.d(TAG, "Login: ", e)
            }
        }
    }
}

class ViewAdapter(classification: Int, var bundle: Bundle? = null) : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {
    private val classification = classification // 0: 관리자, 1: 사용자, 2: 장바구니
    private var items = ArrayList<BookInfo>()
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
                    2 -> {
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
        if (classification == 2) {
            holder.checkBox.isChecked = true
        }
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
            } else if (classification == 2) {    // 장바구니
                totalPrice.text = getTotalPrice()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun cleanItems() { items.clear() }

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
            2 -> {

            }
        }
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