package com.example.sch_library.user

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

private const val TAG = "UserBasket"

class BasketFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: BasketViewAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onResume() {
        super.onResume()

        updateBasketList(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_basket, container, false)

        val orderButton: Button = view.findViewById(R.id.button_order_basket)
        val deleteButton: Button = view.findViewById(R.id.button_delete_basket)
        val totalPrice: TextView = view.findViewById(R.id.textview_total_price)
        val checkAll: CheckBox = view.findViewById(R.id.checkbox_all)

        viewManager = LinearLayoutManager(context)
        viewAdapter = BasketViewAdapter(userId)
        viewAdapter.addViewForBasket(orderButton, deleteButton, totalPrice, checkAll, view.context)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_basket).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        updateBasketList(userId)

        return view
    }

    fun updateBasketList(id: String?) {
        val task = UpdateBasketList()
        task.execute(
            "http://$IP_ADDRESS/select_book_basket.php",
            "select a.도서번호, 도서명, 재고량, 판매가, 수량 from 도서 a, 장바구니담기 b where a.도서번호=b.도서번호 and b.바구니번호 in (select 바구니번호 from 장바구니 where 아이디='$id')"
        )
    }

    fun deleteBasketContents() {
        val task = UpdateBasketList()
        task.execute(
            "http://$IP_ADDRESS/delete_book.php",
            "delete from 장바구니담기 where 바구니번호=(select 바구니번호 from 장바구니 where 아이디='$userId')"
        )
    }

    fun deleteBasket() {
        val task = UpdateBasketList()
        task.execute(
            "http://$IP_ADDRESS/delete_book.php",
            "delete from 장바구니 where 아이디='$userId'"
        )
    }

    inner class UpdateBasketList : AsyncTask<String, Void, String>() {
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
                        val count = item.getString("count")

                        viewAdapter.addItem(BookInfo(bookNumber.toInt(), bookTitle, stock.toInt(), price.toInt(), true, count.toInt()))
                    }

                    viewAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    Log.d(TAG, "Login: ", e)
                }
            }
        }
    }

    inner class BasketViewAdapter(private val id: String?) : RecyclerView.Adapter<BasketViewAdapter.ViewHolder>() {
        private var items = ArrayList<BookInfo>()
        private var count = 0

        private lateinit var deleteButton: Button
        private lateinit var orderButton: Button
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
            totalPrice.text = getTotalPrice()
        }

        override fun getItemCount(): Int = items.size

        fun cleanItems() {
            items.clear()
            totalPrice.text = getTotalPrice()
        }

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

        fun addViewForBasket(orderButton: Button, deleteButton: Button, totalPrice: TextView, checkAll: CheckBox, context: Context) {
            this.orderButton = orderButton
            this.deleteButton = deleteButton
            this.totalPrice = totalPrice
            this.checkAll = checkAll

            orderButton.setOnClickListener {
                val intent = Intent(context, OrderActivity::class.java)
                intent.putExtra("from", "basket")
                val checkItems = ArrayList<BookInfo>()
                for (item in items) {
                    if (item.isSelected) {
                        checkItems.add(item)
                    }
                }
                intent.putExtra("basket", checkItems)
                intent.putExtra("userid", id)
                context.startActivity(intent)
            }

            deleteButton.setOnClickListener {
                var i = 0
                while (i < items.size) {
                    if (items[i].isSelected) {
                        val task = UpdateBasketList()
                        task.execute(
                            "http://$IP_ADDRESS/delete_book.php",
                            "delete from 장바구니담기 where 바구니번호=(select 바구니번호 from 장바구니 where 아이디='$id') and 도서번호=${items[i].number}"
                        )
                        items.removeAt(i)
                        i--
                    }
                    i++
                }
                totalPrice.text = getTotalPrice()
                notifyDataSetChanged()
                Toast.makeText(context, "삭제 완료!", Toast.LENGTH_LONG).show()
            }

            checkAll.isChecked = true
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

        private fun clear() {
            count = 0
            for (item in items) {
                item.isSelected = false
            }
        }
    }
}