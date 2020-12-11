package com.example.sch_library.user

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
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

class OrderDetailsFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var orderViewAdapter: OrderDetailsViewAdapter
    lateinit var viewManager: RecyclerView.LayoutManager

    override fun onResume() {
        super.onResume()

        updateOrderDetails()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_order_details, container, false)

        viewManager = LinearLayoutManager(context)
        orderViewAdapter = OrderDetailsViewAdapter()
        updateOrderDetails()

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

    fun updateOrderDetails() {
        UpdateOrderDetails().execute(
            "http://$IP_ADDRESS/select_order_details.php",
            "select * from 주문 where 아이디='$userId'"
        )
    }

    inner class OrderDetailsViewAdapter : RecyclerView.Adapter<OrderDetailsViewAdapter.ViewHolder>() {
        private val items = ArrayList<OrderInfo>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val orderNumber: TextView = itemView.findViewById(R.id.textview_order_number)
            private val orderDate: TextView = itemView.findViewById(R.id.textview_order_date)
            private val orderTotalPrice: TextView = itemView.findViewById(R.id.textview_order_total_price)
            private val orderStatus: TextView = itemView.findViewById(R.id.textview_order_status)
            private val decideButton: Button = itemView.findViewById(R.id.button_decide)

            init {
                itemView.setOnClickListener {
                    val item = items[adapterPosition]
                    val orderDetailsDialog = OrderDetailsDialog(item)
                    orderDetailsDialog.show()
                }

                decideButton.setOnClickListener {
                    val item = items[adapterPosition]
                    when (decideButton.text) {
                        "주문취소" -> {
                            val builder = AlertDialog.Builder(context)
                            builder.setNegativeButton("아니요") { dialog, id ->
                                dialog.dismiss()
                            }
                            builder.setTitle("주문 취소").setMessage("주문을 취소하시겠습니까?")
                            builder.setPositiveButton("네") { dialog, id ->
                                OrderCancel().execute(
                                    "http://$IP_ADDRESS/order_cancel.php",
                                    item.orderNumber
                                )

                                dialog.dismiss()
                            }
                            val alertDialog = builder.create()
                            alertDialog.show()
                        }
                        "수취확인" -> {
                            UpdateOrderDetails().execute(
                                "http://$IP_ADDRESS/insert_card_address.php",
                                "update 주문 set 주문상태='판매완료' where 주문번호=${item.orderNumber}"
                            )
                        }
                        "기록삭제" -> {
                            val builder = AlertDialog.Builder(context)
                            builder.setNegativeButton("아니요") { dialog, id ->
                                dialog.dismiss()
                            }
                            builder.setTitle("주문 기록 삭제").setMessage("주문 기록을 삭제하시겠습니까?")
                            builder.setPositiveButton("네") { dialog, id ->
                                OrderCancel().execute(
                                    "http://$IP_ADDRESS/delete_order.php",
                                    item.orderNumber
                                )

                                dialog.dismiss()
                            }
                            val alertDialog = builder.create()
                            alertDialog.show()
                        }
                        else -> {}
                    }
                }
            }

            fun setItem(item: OrderInfo) {
                orderNumber.text = item.orderNumber
                orderDate.text = item.orderDate
                orderTotalPrice.text = item.orderTotalPrice.toString()
                orderStatus.text = item.orderStatus

                when (item.orderStatus) {
                    "신청" -> {
                        decideButton.text = "주문취소"
                        orderStatus.setTextColor(Color.GREEN)
                    }
                    "발송" -> {
                        decideButton.text = "수취확인"
                        orderStatus.setTextColor(Color.BLUE)
                    }
                    "판매완료" -> {
                        decideButton.text = "기록삭제"
                        orderStatus.setTextColor(Color.BLACK)
                    }
                    else -> {
                        decideButton.visibility = View.GONE
                        orderStatus.setTextColor(Color.BLACK)
                    }
                }
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

        fun addItem(item: OrderInfo) { items.add(item) }
        fun clear() { items.clear() }
    }

    inner class OrderDetailsDialog(private val item: OrderInfo) : Dialog(context) {
        lateinit var recyclerView: RecyclerView
        lateinit var viewManager: RecyclerView.LayoutManager
        lateinit var viewAdapter: OrderDetailsViewAdapter

        lateinit var zipCode: TextView
        lateinit var basicAddress: TextView
        lateinit var detailAddress: TextView
        lateinit var totalPrice: TextView
        lateinit var cardType: TextView
        lateinit var cardNumber: TextView
        lateinit var cardExpirationDate: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_order_details)

            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params

            viewManager = LinearLayoutManager(context)
            viewAdapter = OrderDetailsViewAdapter()

            UpdateBookList().execute(
                "http://$IP_ADDRESS/select_book_basket.php",
                "select 도서.도서번호, 도서명, 재고량, 판매가, 수량 from 도서, 주문선택 where 도서.도서번호=주문선택.도서번호 and 주문번호=${item.orderNumber}"
            )

            recyclerView = findViewById(R.id.recyclerview_order_books)
            recyclerView.apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter
            }

            zipCode = findViewById(R.id.textview_zip_code)
            basicAddress = findViewById(R.id.textview_basic_address)
            detailAddress = findViewById(R.id.textview_detail_address)
            totalPrice = findViewById(R.id.textview_total_price)
            cardType = findViewById(R.id.textview_card_type)
            cardNumber = findViewById(R.id.textview_card_number)
            cardExpirationDate = findViewById(R.id.textview_card_expiration_date)

            zipCode.text = item.addressInfo.zipCode
            basicAddress.text = item.addressInfo.basicAddress
            detailAddress.text = item.addressInfo.detailAddress
            totalPrice.text = "총 ${item.orderTotalPrice} 원"
            cardType.text = item.cardInfo.type
            cardNumber.text = item.cardInfo.number
            cardExpirationDate.text = item.cardInfo.expiration_date
        }

        override fun onBackPressed() {
            dismiss()
        }

        inner class OrderDetailsViewAdapter : RecyclerView.Adapter<OrderDetailsViewAdapter.ViewHolder>() {
            private var items = ArrayList<BookInfo>()

            inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                private var bookNumber: TextView = itemView.findViewById(R.id.booknumber)
                private var bookTitle: TextView = itemView.findViewById(R.id.booktitle)
                private var bookStock: TextView = itemView.findViewById(R.id.bookstock)
                private var bookPrice: TextView = itemView.findViewById(R.id.bookprice)
                private var bookCount: TextView = itemView.findViewById(R.id.textview_count)
                private var decreaseCountButton: ImageButton = itemView.findViewById(R.id.button_decrease_count)
                private var increaseCountButton: ImageButton = itemView.findViewById(R.id.button_increase_count)
                private var checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

                init {
                    checkBox.visibility = View.GONE

                    decreaseCountButton.visibility = View.INVISIBLE
                    increaseCountButton.visibility = View.INVISIBLE
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

            fun addItem(item: BookInfo) { items.add(item) }
            fun clear() { items.clear() }
        }

        inner class UpdateBookList : AsyncTask<String, Void, String>() {
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
                    return "Error: ${e.message}"
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)

                if (result == "empty") {
                    viewAdapter.clear()
                    viewAdapter.notifyDataSetChanged()
                } else {
                    try {
                        val jsonObject = JSONObject(result)
                        val jsonArray = jsonObject.getJSONArray("result")
                        viewAdapter.clear()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i);
                            val bookNumber = item.getString("booknumber")
                            val bookTitle = item.getString("booktitle")
                            val stock = item.getString("stock")
                            val price = item.getString("price")
                            val count = item.getString("count")

                            viewAdapter.addItem(BookInfo(bookNumber.toInt(), bookTitle, stock.toInt(), price.toInt(), false, count.toInt()))
                        }

                        viewAdapter.notifyDataSetChanged()
                    } catch (e: JSONException) { }
                }
            }
        }
    }

    inner class UpdateOrderDetails : AsyncTask<String, Void, String>() {
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
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            when {
                result?.contains("성공")!! -> {
                    updateOrderDetails()
                }
                result.contains("실패") -> {

                }
                result == "empty" -> {
                    orderViewAdapter.clear()
                    orderViewAdapter.notifyDataSetChanged()
                }
                else -> {
                    try {
                        val jsonObject = JSONObject(result)
                        val jsonArray = jsonObject.getJSONArray("result")

                        orderViewAdapter.clear()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val orderNumber = item.getString("ordernumber")
                            val orderDate = item.getString("orderdate")
                            val orderTotalPrice = item.getString("ordertotalprice")
                            val orderStatus = item.getString("orderstatus")
                            val cardNumber = item.getString("cardnumber")
                            val cardExpirationDate = item.getString("cardexpirationdate")
                            val cardType = item.getString("cardtype")
                            val zipCode = item.getString("zipcode")
                            val basicAddress = item.getString("basicaddress")
                            val detailAddress = item.getString("detailaddress")

                            val orderInfo = OrderInfo(
                                orderNumber, orderDate, orderTotalPrice.toInt(), orderStatus,
                                CardInfo(cardType, cardNumber, cardExpirationDate),
                                AddressInfo(zipCode, basicAddress, detailAddress)
                            )
                            orderViewAdapter.addItem(orderInfo)
                            orderViewAdapter.notifyDataSetChanged()
                        }
                    } catch (e: JSONException) { }
                }
            }
        }
    }

    inner class OrderCancel : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val orderNumber = p0[1]

            val serverURL = p0[0]
            val postParameters = "ordernumber=$orderNumber"

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
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (result == "성공") {
                updateOrderDetails()
            }
        }
    }
}