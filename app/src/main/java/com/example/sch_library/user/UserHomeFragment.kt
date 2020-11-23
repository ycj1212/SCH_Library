package com.example.sch_library.user

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import com.example.sch_library.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


private const val TAG = "UserHome"

interface OnSetHomeViewAdapterListener {
    fun onSetHomeViewAdapter(viewAdapter: HomeViewAdapter)
}

class UserHomeFragment : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: HomeViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var basketButton: Button
    private lateinit var orderButton: Button
    private lateinit var selectedCount: TextView
    private lateinit var userHomeButtonsLayout: LinearLayout

    lateinit var onSetHomeViewAdapterListener: OnSetHomeViewAdapterListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)

        val bundle = arguments
        val id = bundle?.getString("id")
        // 도서명, 도서번호
        // 도서명, 도서번호, 가격 오름차순, 내림차순

        basketButton = view.findViewById(R.id.button_basket)
        orderButton = view.findViewById(R.id.button_order)
        selectedCount = view.findViewById(R.id.textview_selected_count)
        userHomeButtonsLayout = view.findViewById(R.id.layout_user_home_buttons)

        viewManager = LinearLayoutManager(context)
        viewAdapter = HomeViewAdapter(id)
        viewAdapter.addViewForUser(basketButton, orderButton, userHomeButtonsLayout, selectedCount)

        val task = UpdateBookList(context!!)
        task.execute("http://$IP_ADDRESS/select_book.php", "select * from 도서")

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_booklist).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        onSetHomeViewAdapterListener = context as OnSetHomeViewAdapterListener
        onSetHomeViewAdapterListener.onSetHomeViewAdapter(viewAdapter)

        searchView = view.findViewById(R.id.searchview)
        searchView.setOnClickListener {
            searchView.onActionViewExpanded()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                val task = UpdateBookList(context!!)
                task.execute("http://$IP_ADDRESS/select_book.php", "select * from 도서 where 도서명 like \"%$p0%\"")
                viewAdapter.notifyDataSetChanged()
                searchView.clearFocus()

                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                val task = UpdateBookList(context!!)
                task.execute("http://$IP_ADDRESS/select_book.php", "select * from 도서 where 도서명 like \"%$p0%\"")
                Toast.makeText(context, "검색중", Toast.LENGTH_SHORT).show()

                viewAdapter.notifyDataSetChanged()

                return false
            }
        })

        return view
    }

    inner class UpdateBookList(private val context: Context) : AsyncTask<String, Void, String>() {
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

            if (result == "empty") {
                viewAdapter.cleanItems()
                viewAdapter.notifyDataSetChanged()
            } else {
                try {
                    val jsonObject = JSONObject(result)
                    val jsonArray = jsonObject.getJSONArray("result")
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
}

class HomeViewAdapter(private val id: String?) : RecyclerView.Adapter<HomeViewAdapter.ViewHolder>() {
    private val items = ArrayList<BookInfo>()
    private var count = 0

    private lateinit var basketButton: Button
    private lateinit var orderButton: Button
    private lateinit var userHomeButtonsLayout: LinearLayout
    private lateinit var selectedCount: TextView

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
                val tempBookInfo = items[adapterPosition]
                val dialog = BookInfoDialog(itemView.context, tempBookInfo, id)
                /*
                dialog.setDialogListener(object : BookInfoDialogListener {
                    override fun onClicked() {
                        notifyDataSetChanged()
                    }
                })
                */
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
                userHomeButtonsLayout.visibility = View.GONE
            } else {
                userHomeButtonsLayout.visibility = View.VISIBLE
                selectedCount.text = "${count}개 선택됨"
            }
        }
    }

    override fun getItemCount(): Int = items.size
    fun getSelectedCount(): Int = count
    fun addItem(item: BookInfo) { items.add(item) }
    fun cleanItems() { items.clear() }
    fun clear() {
        count = 0
        for (item in items) {
            item.isSelected = false
        }
        userHomeButtonsLayout.visibility = View.GONE
    }

    fun addViewForUser(basketButton: Button, orderButton: Button, layout: LinearLayout, textView: TextView) {
        this.basketButton = basketButton
        this.orderButton = orderButton
        this.userHomeButtonsLayout = layout
        this.selectedCount = textView
    }
}

interface BookInfoDialogListener {
    fun onClicked()
}

class BookInfoDialog(context: Context, private val bookInfo: BookInfo, private val id: String?) : Dialog(context) {
    lateinit var title: TextView
    lateinit var number: TextView
    lateinit var stock: TextView
    lateinit var price: TextView

    lateinit var count: TextView
    lateinit var decreaseCount: ImageButton
    lateinit var increaseCount: ImageButton

    //lateinit var bookInfoDialogListener: BookInfoDialogListener

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
            //
        }

        val orderButton: Button = findViewById(R.id.button_order)
        orderButton.setOnClickListener {
            val intent = Intent(context, OrderActivity::class.java)
            intent.putExtra("from", "bookinfodialog")
            intent.putExtra("booknumber", bookInfo.number)
            intent.putExtra("booktitle", bookInfo.title)
            intent.putExtra("bookstock", bookInfo.stock)
            intent.putExtra("bookprice", bookInfo.price)
            intent.putExtra("bookcount", bookInfo.count)
            intent.putExtra("userid", id)
            context.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        //bookInfoDialogListener.onClicked()
        dismiss()
    }

    /*
    fun setDialogListener(listener: BookInfoDialogListener) {
        bookInfoDialogListener = listener
    }
    */
}