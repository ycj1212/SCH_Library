package com.example.sch_library.user

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

private const val TAG = "Order"

class OrderActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: OrderViewAdapter
    lateinit var viewManager: RecyclerView.LayoutManager

    lateinit var totalPrice: TextView

    lateinit var zipCode: EditText
    lateinit var basicAddress: EditText
    lateinit var detailAddress: EditText

    lateinit var cardType: EditText
    lateinit var cardNumber: EditText
    lateinit var cardExpirationDate: EditText

    lateinit var addressRadioGroup: RadioGroup
    lateinit var homeRadioButton: RadioButton
    lateinit var rectalRadioButton: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        val intent = intent

        val userid = intent.getStringExtra("userid")
        /*
        val userInfo = intent.getBundleExtra("userinfo")
        val userid = userInfo.getString("id")
        val userpw = userInfo.getString("pw")
        val username = userInfo.getString("name")
         */

        viewManager = LinearLayoutManager(applicationContext)
        viewAdapter = OrderViewAdapter()

        recyclerView = findViewById(R.id.recyclerview_order)
        recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        totalPrice = findViewById(R.id.textview_total_price)
        zipCode = findViewById(R.id.edittext_zip_code)
        basicAddress = findViewById(R.id.edittext_basic_address)
        detailAddress = findViewById(R.id.edittext_detail_address)
        cardType = findViewById(R.id.edittext_card_type)
        cardNumber = findViewById(R.id.edittext_card_number)
        cardExpirationDate = findViewById(R.id.edittext_card_expiration_date)

        viewAdapter.addView(totalPrice)

        when (intent.getStringExtra("from")) {
            "bookinfodialog" -> {
                val bookNumber = intent.getIntExtra("booknumber", 0)
                val bookTitle = intent.getStringExtra("booktitle")
                val bookStock = intent.getIntExtra("bookstock", 0)
                val bookPrice = intent.getIntExtra("bookprice", 0)
                val bookCount = intent.getIntExtra("bookcount", 0)

                viewAdapter.addItem(BookInfo(bookNumber, bookTitle, bookStock, bookPrice, count=bookCount))
                viewAdapter.notifyDataSetChanged()
            }
            "userhome" -> {

            }
            "basket" -> {
                val items = intent.getSerializableExtra("basket") as ArrayList<BookInfo>

                viewAdapter.setItems(items)
                viewAdapter.notifyDataSetChanged()
            }
        }

        addressRadioGroup = findViewById(R.id.radiogroup_address)
        homeRadioButton = findViewById(R.id.radiobutton_home)
        rectalRadioButton = findViewById(R.id.radiobutton_rectal)

        addressRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.radiobutton_home -> {
                    val task = UpdateOrderInfo(applicationContext)
                    task.execute("http://$IP_ADDRESS/select_order.php", "address", "select * from 회원주소 where 아이디=\"$userid\" && 배송지=\"집\"")
                }
                R.id.radiobutton_rectal -> {
                    val task = UpdateOrderInfo(applicationContext)
                    task.execute("http://$IP_ADDRESS/select_order.php", "address", "select * from 회원주소 where 아이디=\"$userid\" && 배송지=\"직장\"")
                }
            }
        }

        val updateAddressButton: Button = findViewById(R.id.button_update_address)
        updateAddressButton.setOnClickListener {
            val task = UpdateOrderInfo(applicationContext)
            task.execute("http://$IP_ADDRESS/select_order.php", "address", "update 회원주소 set 우편번호='', 기본주소='', 상세주소='' where 아이디=\"$userid\" && 배송지=''")
        }

        val selectCardButton: Button = findViewById(R.id.button_select_card)
        selectCardButton.setOnClickListener {
            val task = UpdateOrderInfo(applicationContext)
            task.execute("http://$IP_ADDRESS/select_order.php", "card", "select * from 회원카드 where 아이디=\"$userid\"")
        }

        val addCardButton: Button = findViewById(R.id.button_add_card)
        addCardButton.setOnClickListener {

        }

        val payButton: Button = findViewById(R.id.button_pay)
        payButton.setOnClickListener {

        }
    }

    class UpdateOrderInfo(private val context: Context) : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val kind = p0[1]
            val query = p0[2]

            val serverURL = p0[0]
            val postParameters = "kind=$kind&query=$query"

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
                val jsonArray = jsonObject.getJSONArray("result")

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i);
                    val bookNumber = item.getString("booknumber")
                    val bookTitle = item.getString("booktitle")
                    val stock = item.getString("stock")
                    val price = item.getString("price")
                }
            } catch (e: JSONException) {
                Log.d(TAG, "Login: ", e)
            }
        }
    }
}

class OrderViewAdapter : RecyclerView.Adapter<OrderViewAdapter.ViewHolder>() {
    private var items = ArrayList<BookInfo>()
    private var count = 0

    private lateinit var totalPrice: TextView

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

            }

            checkBox.visibility = View.GONE

            decreaseCountButton.setOnClickListener {
                if (items[adapterPosition].count > 1) {
                    items[adapterPosition].count--
                    bookCount.text = items[adapterPosition].count.toString()
                    totalPrice.text = getTotalPrice()
                }
            }

            increaseCountButton.setOnClickListener {
                if (items[adapterPosition].count < items[adapterPosition].stock) {
                    items[adapterPosition].count++
                    bookCount.text = items[adapterPosition].count.toString()
                    totalPrice.text = getTotalPrice()
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
        holder.checkBox.isChecked = true
        holder.checkBox.setOnClickListener {
            count = 0
            item.isSelected = holder.checkBox.isChecked

            for (item in items) {
                if (item.isSelected) {
                    count++
                }
            }

            totalPrice.text = getTotalPrice()
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: ArrayList<BookInfo>) {
        this.items.clear()
        for (item in items) {
            this.items.add(item)
        }
    }

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

    fun addView(totalPrice: TextView) {
        this.totalPrice = totalPrice
    }

    fun getSelectedCount(): Int = count

    fun clear() {
        count = 0
        for (item in items) {
            item.isSelected = false
        }
    }
}