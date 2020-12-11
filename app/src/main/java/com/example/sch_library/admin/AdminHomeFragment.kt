package com.example.sch_library.admin

import android.app.Dialog
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
import com.example.sch_library.user.userId
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "AdminHome"

class AdminHomeFragment : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var viewAdapter: ViewAdapter

    lateinit var addButton: Button
    lateinit var deleteButton: Button

    override fun onResume() {
        super.onResume()

        UpdateBookList().execute(
            "http://$IP_ADDRESS/select_book.php",
            "select * from 도서",
            "select"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)

        addButton = view.findViewById(R.id.button_add_book)
        addButton.setOnClickListener {
            val bookAddDialog = BookAddDialog()
            bookAddDialog.show()
        }

        deleteButton = view.findViewById(R.id.button_delete_selected)
        deleteButton.setOnClickListener {
            viewAdapter.deleteSelectedItems()
        }

        viewManager = LinearLayoutManager(context)
        viewAdapter = ViewAdapter()

        UpdateBookList().execute(
            "http://$IP_ADDRESS/select_book.php",
            "select * from 도서",
            "select"
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_booklist).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        searchView = view.findViewById(R.id.searchview)
        searchView.setOnClickListener {
            searchView.onActionViewExpanded()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                UpdateBookList().execute(
                    "http://$IP_ADDRESS/select_book.php",
                    "select * from 도서 where 도서명 like \"%$p0%\"",
                    "select"
                )
                searchView.clearFocus()

                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                UpdateBookList().execute(
                    "http://$IP_ADDRESS/select_book.php",
                    "select * from 도서 where 도서명 like \"%$p0%\"",
                    "select"
                )

                return false
            }
        })

        return view
    }

    inner class ViewAdapter : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {
        private var items = ArrayList<BookInfo>()
        private var count = 0

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
                    val dialog = BookUpdateDialog(items[adapterPosition])
                    dialog.show()
                }

                decreaseCountButton.setOnClickListener {
                    if (items[adapterPosition].count > 1) {
                        items[adapterPosition].count--
                        bookCount.text = items[adapterPosition].count.toString()
                    }
                }

                increaseCountButton.setOnClickListener {
                    if (items[adapterPosition].count < items[adapterPosition].stock) {
                        items[adapterPosition].count++
                        bookCount.text = items[adapterPosition].count.toString()
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

        fun getSelectedCount() = count
        fun deleteSelectedItems() {
            var i = 0
            while (i < items.size) {
                if (items[i].isSelected) {
                    DeleteBookNumber().execute(
                        "http://$IP_ADDRESS/delete_booknumber.php",
                        items[i].number.toString()
                    )
                    items.removeAt(i)
                    i--
                }
                i++
            }
            count = 0

            for (item in items) {
                item.isSelected = false
            }

            addButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE
        }
        fun cleanItems() {
            items.clear()
            count = 0
            addButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE
        }
        fun addItem(item: BookInfo) {
            items.add(item)
        }
        fun clear() {
            count = 0
            for (item in items) {
                item.isSelected = false
            }

            addButton.visibility = View.VISIBLE
            deleteButton.visibility = View.GONE
        }
    }

    inner class BookAddDialog : Dialog(context) {
        lateinit var title: EditText
        lateinit var number: EditText
        lateinit var stock: EditText
        lateinit var price: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_add_book)

            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params

            title = findViewById(R.id.edittext_book_title)
            number = findViewById(R.id.edittext_book_number)
            stock = findViewById(R.id.edittext_book_stock)
            price = findViewById(R.id.edittext_book_price)

            val addButton: Button = findViewById(R.id.button_add_book)
            addButton.setOnClickListener {
                UpdateBookList().execute(
                    "http://$IP_ADDRESS/insert_card_address.php",
                    "insert into 도서 values (${number.text.toString()}, '${title.text.toString()}', ${stock.text.toString()}, ${price.text.toString()})",
                    "insert"
                )
                dismiss()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class BookUpdateDialog(private val bookInfo: BookInfo) : Dialog(context) {
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
            number.setText(bookInfo.number.toString())
            stock.setText(bookInfo.stock.toString())
            price.setText(bookInfo.price.toString())

            val updateButton: Button = findViewById(R.id.button_update_book)
            updateButton.setOnClickListener {
                if (number.text.toString() == bookInfo.number.toString()) {
                    UpdateBookList().execute(
                        "http://$IP_ADDRESS/insert_card_address.php",
                        "update 도서 set 도서번호=${number.text.toString()}, 도서명='${title.text.toString()}', 재고량=${stock.text.toString()}, 판매가=${price.text.toString()} where 도서번호=${bookInfo.number}",
                        "update"
                    )
                } else {
                    UpdateBookNumber().execute(
                        "http://$IP_ADDRESS/update_booknumber.php",
                        number.text.toString(),
                        title.text.toString(),
                        stock.text.toString(),
                        price.text.toString(),
                        bookInfo.number.toString()
                    )
                }
                dismiss()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class UpdateBookList : AsyncTask<String, Void, String>() {
        var type = ""
        override fun doInBackground(vararg p0: String?): String {
            val query = p0[1]
            type = p0[2]!!

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

            if (type == "select") {
                viewAdapter.cleanItems()
                try {
                    val jsonObject = JSONObject(result)
                    val jsonArray = jsonObject.getJSONArray("result")

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i);
                        val bookNumber = item.getString("booknumber")
                        val bookTitle = item.getString("booktitle")
                        val stock = item.getString("stock")
                        val price = item.getString("price")

                        viewAdapter.addItem(BookInfo(bookNumber.toInt(), bookTitle, stock.toInt(), price.toInt()))
                    }
                } catch (e: JSONException) {
                    Log.d(TAG, "Login: ", e)
                }
                viewAdapter.notifyDataSetChanged()
            } else {
                if (result?.contains("성공")!!) {
                    UpdateBookList().execute(
                        "http://$IP_ADDRESS/select_book.php",
                        "select * from 도서",
                        "select"
                    )
                }
            }
        }
    }

    inner class UpdateBookNumber : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val newNumber = p0[1]
            val newTitle = p0[2]
            val newStock = p0[3]
            val newPrice = p0[4]
            val oldNumber = p0[5]
            val serverURL = p0[0]
            val postParameters = "newnumber=$newNumber&newtitle=$newTitle&newstock=$newStock&newprice=$newPrice&oldnumber=$oldNumber"

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

            if (result == "성공") {
                UpdateBookList().execute(
                        "http://$IP_ADDRESS/select_book.php",
                        "select * from 도서",
                        "select"
                )
            }
        }
    }

    inner class DeleteBookNumber : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val bookNumber = p0[1]
            val serverURL = p0[0]
            val postParameters = "booknumber=$bookNumber"

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

            if (result?.contains("성공")!!) {
                UpdateBookList().execute(
                    "http://$IP_ADDRESS/select_book.php",
                    "select * from 도서",
                    "select"
                )
            }
        }
    }
}