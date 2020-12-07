package com.example.sch_library.user

import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.BookInfo
import com.example.sch_library.CardInfo
import com.example.sch_library.IP_ADDRESS
import com.example.sch_library.R
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "Order"

class OrderActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var orderViewAdapter: OrderViewAdapter
    lateinit var viewManager: RecyclerView.LayoutManager

    lateinit var cardViewAdapter: CardListDialog.CardAdapter
    lateinit var noCardTextView: TextView

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

    var userId = ""
    var orderNumber = 0
    val orderNumberList = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        val intent = intent

        userId = intent.getStringExtra("userid")

        viewManager = LinearLayoutManager(this)
        orderViewAdapter = OrderViewAdapter()

        recyclerView = findViewById(R.id.recyclerview_order)
        recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = orderViewAdapter
        }

        totalPrice = findViewById(R.id.textview_total_price)
        zipCode = findViewById(R.id.edittext_zip_code)
        basicAddress = findViewById(R.id.edittext_basic_address)
        detailAddress = findViewById(R.id.edittext_detail_address)

        cardType = findViewById(R.id.edittext_card_type)
        cardNumber = findViewById(R.id.edittext_card_number)
        cardExpirationDate = findViewById(R.id.edittext_card_expiration_date)

        orderViewAdapter.addView(totalPrice)

        when (intent.getStringExtra("from")) {
            "bookinfodialog" -> {
                val bookNumber = intent.getIntExtra("booknumber", 0)
                val bookTitle = intent.getStringExtra("booktitle")
                val bookStock = intent.getIntExtra("bookstock", 0)
                val bookPrice = intent.getIntExtra("bookprice", 0)
                val bookCount = intent.getIntExtra("bookcount", 0)

                orderViewAdapter.addItem(BookInfo(bookNumber, bookTitle, bookStock, bookPrice, count=bookCount))
                orderViewAdapter.notifyDataSetChanged()
            }
            "userhome" -> {
                val items = intent.getSerializableExtra("items") as ArrayList<BookInfo>

                orderViewAdapter.setItems(items)
                orderViewAdapter.notifyDataSetChanged()
            }
            "basket" -> {
                val items = intent.getSerializableExtra("basket") as ArrayList<BookInfo>

                orderViewAdapter.setItems(items)
                orderViewAdapter.notifyDataSetChanged()
            }
        }

        addressRadioGroup = findViewById(R.id.radiogroup_address)
        homeRadioButton = findViewById(R.id.radiobutton_home)
        rectalRadioButton = findViewById(R.id.radiobutton_rectal)

        addressRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.radiobutton_home -> {
                    val task = UpdateOrderInfo()
                    task.execute(
                        "http://$IP_ADDRESS/select_order.php",
                        "address",
                        "select * from 회원주소 where 아이디='$userId' and 배송지='자택'"
                    )
                }
                R.id.radiobutton_rectal -> {
                    val task = UpdateOrderInfo()
                    task.execute(
                        "http://$IP_ADDRESS/select_order.php",
                        "address",
                        "select * from 회원주소 where 아이디='$userId' and 배송지='직장'"
                    )
                }
            }
        }

        val updateAddressButton: Button = findViewById(R.id.button_update_address)
        updateAddressButton.setOnClickListener {
            val addressType = if (addressRadioGroup.checkedRadioButtonId == R.id.radiobutton_home) {
                "자택"
            } else {
                "직장"
            }
            val task = UpdateOrderInfo()
            task.execute(
                "http://$IP_ADDRESS/insert_card_address.php",
                "address",
                "update 회원주소 set 우편번호='${zipCode.text.toString()}', 기본주소='${basicAddress.text.toString()}', 상세주소='${detailAddress.text.toString()}' where 아이디='$userId' and 배송지='$addressType'"
            )
        }

        val selectCardButton: Button = findViewById(R.id.button_select_card)
        selectCardButton.setOnClickListener {
            CardListDialog().show()
        }

        val task = UpdateOrderInfo()
        task.execute(
            "http://$IP_ADDRESS/select_order.php",
            "order",
            "select 주문번호 from 주문"
        )

        val payButton: Button = findViewById(R.id.button_pay)
        payButton.setOnClickListener {
            while (!getOrderNumber()) { }

            val expirationDateTemp = cardExpirationDate.text.toString().split('/')
            val expirationYear = expirationDateTemp[1]
            val expirationMonth = expirationDateTemp[0].toInt()
            var expirationDate = "$expirationYear/$expirationMonth"

            if (expirationMonth == 1 or 3 or 5 or 7 or 8 or 10 or 12) {
                expirationDate += "/31"
            } else if (expirationMonth == 4 or 6 or 9 or 11) {
                expirationDate += "/30"
            } else {
                expirationDate += "/28"
            }

            val task = InsertOrderInfo(0)
            task.execute(
                "http://$IP_ADDRESS/insert_order.php",
                orderViewAdapter.getTotalPrice().toString(),
                cardType.text.toString(),
                cardNumber.text.toString(),
                expirationDate,
                zipCode.text.toString(),
                basicAddress.text.toString(),
                detailAddress.text.toString(),
                orderNumber.toString()
            )
        }
    }

    private fun getOrderNumber(): Boolean {
        val now = LocalDate.now()
        val format = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()

        val random = Random().nextInt(100)
        orderNumber = format * 100 + random
        for (i in 0 until orderNumberList.size) {
            if (orderNumber == orderNumberList[i]) {
                return false
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    class OrderViewAdapter : RecyclerView.Adapter<OrderViewAdapter.ViewHolder>() {
        private var items = ArrayList<BookInfo>()

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
                checkBox.visibility = View.GONE

                decreaseCountButton.setOnClickListener {
                    if (items[adapterPosition].count > 1) {
                        items[adapterPosition].count--
                        bookCount.text = items[adapterPosition].count.toString()
                        totalPrice.text = "총 ${getTotalPrice()} 원"
                    }
                }

                increaseCountButton.setOnClickListener {
                    if (items[adapterPosition].count < items[adapterPosition].stock) {
                        items[adapterPosition].count++
                        bookCount.text = items[adapterPosition].count.toString()
                        totalPrice.text = "총 ${getTotalPrice()} 원"
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
        }

        override fun getItemCount(): Int = items.size

        fun setItems(items: ArrayList<BookInfo>) {
            this.items.clear()
            for (item in items) {
                this.items.add(item)
            }
            totalPrice.text = "총 ${getTotalPrice()} 원"
        }

        fun getTotalPrice(): Int {
            var sum = 0
            for (item in items) {
                sum += item.price * item.count
            }
            return sum
        }

        fun addItem(item: BookInfo) {
            items.add(item)
        }

        fun addView(totalPrice: TextView) {
            this.totalPrice = totalPrice
        }

        fun getItems(): ArrayList<BookInfo> = items
    }

    inner class CardListDialog : Dialog(this) {
        lateinit var recyclerView: RecyclerView
        lateinit var viewManager: RecyclerView.LayoutManager
        lateinit var addCardButton: Button

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_cardlist)

            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params

            viewManager = LinearLayoutManager(this@OrderActivity)
            cardViewAdapter = CardAdapter()

            val task = UpdateOrderInfo()
            task.execute(
                "http://$IP_ADDRESS/select_card.php",
                "card",
                "select * from 회원카드 where 아이디='$userId'"
            )

            recyclerView = findViewById(R.id.recyclerview_cardlist)
            recyclerView.apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = cardViewAdapter
            }

            noCardTextView = findViewById(R.id.textview_no_card_cardlist)

            addCardButton = findViewById(R.id.button_add_card_cardlist)
            addCardButton.setOnClickListener {
                val cardAddDialog = CardAddDialog()
                cardAddDialog.show()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }

        inner class CardAdapter() : RecyclerView.Adapter<CardAdapter.ViewHolder>() {
            private val items = ArrayList<CardInfo>()

            inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                private var itemCardType: TextView = itemView.findViewById(R.id.textview_card_type)
                private var itemCardNumber: TextView = itemView.findViewById(R.id.textview_card_number)
                private var itemCardExpirationDate: TextView = itemView.findViewById(R.id.textview_card_expiration_date)

                init {
                    itemView.setOnClickListener {
                        val item = items[adapterPosition]
                        cardType.setText(item.type)
                        cardNumber.setText(item.number)
                        cardExpirationDate.setText(item.expiration_date)
                        dismiss()
                    }
                }

                fun setItem(item: CardInfo) {
                    itemCardType.text = item.type
                    itemCardNumber.text = item.number
                    itemCardExpirationDate.text = item.expiration_date
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardAdapter.ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val itemView = inflater.inflate(R.layout.info_card, parent, false)

                return ViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val item = items[position]
                holder.setItem(item)
            }

            override fun getItemCount(): Int = items.size

            fun addItem(item: CardInfo) { items.add(item) }
            fun cleanItems() { items.clear() }
        }
    }

    inner class CardAddDialog() : Dialog(this) {
        lateinit var cardType: EditText
        lateinit var cardNumber: EditText
        lateinit var cardExpirationDate: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_add_card)

            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params

            cardType = findViewById(R.id.edittext_card_type)
            cardNumber = findViewById(R.id.edittext_card_number)
            cardExpirationDate = findViewById(R.id.edittext_card_expiration_date)

            val cardAddButton: Button = findViewById(R.id.button_add_card)
            cardAddButton.setOnClickListener {
                val expirationDateTemp = cardExpirationDate.text.toString().split('/')
                val expirationYear = expirationDateTemp[1]
                val expirationMonth = expirationDateTemp[0].toInt()
                var expirationDate = "$expirationYear/$expirationMonth"

                if (expirationMonth == 1 or 3 or 5 or 7 or 8 or 10 or 12) {
                    expirationDate += "/31"
                } else if (expirationMonth == 4 or 6 or 9 or 11) {
                    expirationDate += "/30"
                } else {
                    expirationDate += "/28"
                }

                val task = UpdateOrderInfo()
                task.execute(
                    "http://$IP_ADDRESS/insert_card_address.php",
                    "card",
                    "insert into 회원카드 values ('$userId', ${cardNumber.text.toString()}, '$expirationDate', '${cardType.text.toString()}')"
                )
                dismiss()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class UpdateOrderInfo : AsyncTask<String, Void, String>() {
        private var kind = ""
        override fun doInBackground(vararg p0: String?): String {
            kind = p0[1]!!
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

            when {
                result?.contains("성공")!! -> {
                    Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                    if (kind == "card") {
                        val task = UpdateOrderInfo()
                        task.execute(
                            "http://$IP_ADDRESS/select_card.php",
                            "card",
                            "select * from 회원카드 where 아이디='$userId'"
                        )
                    }
                }
                result.contains("실패") -> {
                    Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                }
                result == "empty" -> {
                    if (kind == "card") {
                        cardViewAdapter.cleanItems()
                        cardViewAdapter.notifyDataSetChanged()
                    }
                }
                else -> {
                    try {
                        val jsonObject = JSONObject(result)
                        val jsonArray = jsonObject.getJSONArray("result")

                        if (kind == "address") {
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i);
                                val zipCodeTemp = item.getString("zipcode")
                                val basicAddressTemp = item.getString("basicaddress")
                                val detailAddressTemp = item.getString("detailaddress")

                                zipCode.setText(zipCodeTemp)
                                basicAddress.setText(basicAddressTemp)
                                detailAddress.setText(detailAddressTemp)
                            }
                        } else if (kind == "card") {
                            cardViewAdapter.cleanItems()
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i);
                                val cardType = item.getString("cardtype")
                                val cardNumber = item.getString("cardnumber")
                                val cardExpirationDate = item.getString("cardexpirationdate")
                                val cardExpirationDateTemp = cardExpirationDate.split('-')
                                var cardExpirationYear = cardExpirationDateTemp[0].substring(2,4)
                                val expirationDate = "${cardExpirationDateTemp[1]}/$cardExpirationYear"

                                cardViewAdapter.addItem(CardInfo(cardType, cardNumber, expirationDate))
                            }
                            cardViewAdapter.notifyDataSetChanged()

                            if (cardViewAdapter.itemCount == 0) {
                                noCardTextView.visibility = View.VISIBLE
                            } else {
                                noCardTextView.visibility = View.GONE
                            }
                        } else if (kind == "order") {
                            orderNumberList.clear()
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i);
                                val orderNumber = item.getString("ordernumber")

                                orderNumberList.add(orderNumber.toInt())
                            }
                        }
                    } catch (e: JSONException) {
                        Log.d(TAG, "Login: ", e)
                    }
                }
            }
        }
    }

    inner class InsertOrderInfo(private val type: Int) : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val serverURL = p0[0]
            var postParameters = "type=$type"

            if (type == 0) {
                val totalPrice = p0[1]
                val cardType = p0[2]
                val cardNumber = p0[3]
                val cardExpirationDate = p0[4]
                val zipCode = p0[5]
                val basicAddress = p0[6]
                val detailAddress = p0[7]
                val orderNumber = p0[8]

                postParameters += "&totalprice=$totalPrice&cardtype=$cardType&cardnumber=$cardNumber&cardexpirationdate=$cardExpirationDate"
                postParameters += "&zipcode=$zipCode&basicaddress=$basicAddress&detailaddress=$detailAddress&id=$userId&ordernumber=$orderNumber"
            } else {
                val orderNumber = p0[1]
                val bookNumber = p0[2]
                val bookCount = p0[3]

                postParameters += "&ordernumber=$orderNumber&booknumber=$bookNumber&bookcount=$bookCount"
            }

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

            when {
                result?.contains("주문 성공")!! -> {
                    val items = orderViewAdapter.getItems()
                    for (item in items) {
                        val task = InsertOrderInfo(1)
                        task.execute(
                            "http://$IP_ADDRESS/insert_order.php",
                            orderNumber.toString(),
                            item.number.toString(),
                            item.count.toString()
                        )
                    }
                }
                result.contains("실패") -> {
                    Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
                }
                else -> {
                    Handler().postDelayed({
                        Toast.makeText(applicationContext, "주문 완료!", Toast.LENGTH_LONG).show()
                    }, 500)
                    fragmentOrderDetails.updateOrderDetails()
                    fragmentBasket.deleteBasketContents()
                    fragmentBasket.deleteBasket()
                    finish()
                }
            }
        }
    }
}