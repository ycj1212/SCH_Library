package com.example.sch_library.admin

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.BookInfo
import com.example.sch_library.IP_ADDRESS
import com.example.sch_library.R
import com.example.sch_library.ViewAdapter
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