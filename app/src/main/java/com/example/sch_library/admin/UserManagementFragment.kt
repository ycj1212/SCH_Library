package com.example.sch_library.admin

import android.app.Dialog
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
import com.example.sch_library.IP_ADDRESS
import com.example.sch_library.R
import com.example.sch_library.UserInfo
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class UserManagementFragment : Fragment() {
    lateinit var searchView: SearchView
    lateinit var recyclerView: RecyclerView
    lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var viewAdapter: UserManagementViewAdapter

    override fun onResume() {
        super.onResume()

        UpdateUserManagement().execute(
            "http://$IP_ADDRESS/select_user.php",
            "select * from 회원"
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_management, container, false)

        viewManager = LinearLayoutManager(context)
        viewAdapter = UserManagementViewAdapter()
        recyclerView = view.findViewById(R.id.recyclerview_user_management)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        UpdateUserManagement().execute(
            "http://$IP_ADDRESS/select_user.php",
            "select * from 회원"
        )

        searchView = view.findViewById(R.id.searchview)
        searchView.setOnClickListener {
            searchView.onActionViewExpanded()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                UpdateUserManagement().execute(
                    "http://$IP_ADDRESS/select_user.php",
                    "select * from 회원 where 아이디 like '%$p0%'"
                )
                searchView.clearFocus()

                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                UpdateUserManagement().execute(
                    "http://$IP_ADDRESS/select_user.php",
                    "select * from 회원 where 아이디 like '%$p0%'"
                )

                return false
            }
        })

        return view
    }

    inner class UserManagementViewAdapter : RecyclerView.Adapter<UserManagementViewAdapter.ViewHolder>() {
        val items = ArrayList<UserInfo>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val userId: TextView = itemView.findViewById(R.id.textview_user_id)
            private val userPw: TextView = itemView.findViewById(R.id.textview_user_pw)
            private val userName: TextView = itemView.findViewById(R.id.textview_user_name)
            private val updateUserButton: Button = itemView.findViewById(R.id.button_update_user)
            private val deleteUserButton: Button = itemView.findViewById(R.id.button_delete_user)

            init {
                updateUserButton.setOnClickListener {
                    val item = items[adapterPosition]
                    UserManagementDialog(item).show()
                }

                deleteUserButton.setOnClickListener {
                    val item = items[adapterPosition]
                    DeleteUser().execute(
                        "http://$IP_ADDRESS/delete_user.php",
                        item.userId
                    )
                }
            }

            fun setItem(item: UserInfo) {
                userId.text = item.userId
                userPw.text = item.userPw
                userName.text = item.userName
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.info_user, parent, false)

            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.setItem(item)
        }

        override fun getItemCount(): Int = items.size

        fun clear() { items.clear() }
        fun addItem(item: UserInfo) { items.add(item) }
    }

    inner class UserManagementDialog(private val item: UserInfo) : Dialog(context) {
        lateinit var userId: EditText
        lateinit var userPw: EditText
        lateinit var userName: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_update_user)

            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params

            userId = findViewById(R.id.edittext_user_id)
            userPw = findViewById(R.id.edittext_user_pw)
            userName = findViewById(R.id.edittext_user_name)

            userId.setText(item.userId)
            userPw.setText(item.userPw)
            userName.setText(item.userName)

            val updateButton: Button = findViewById(R.id.button_update_user)
            updateButton.setOnClickListener {
                if (userId.text.toString() == item.userId) {
                    UpdateUserManagement().execute(
                        "http://$IP_ADDRESS/insert_card_address.php",
                        "update 회원 set 아이디='${userId.text}', 비밀번호='${userPw.text}', 이름='${userName.text}' where 아이디='${item.userId}'"
                    )
                } else {
                    UpdateTest().execute(
                        "http://$IP_ADDRESS/test.php",
                        userId.text.toString(),
                        userPw.text.toString(),
                        userName.text.toString(),
                        item.userId
                    )
                    dismiss()
                }
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class UpdateUserManagement : AsyncTask<String, Void, String>() {
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
                    UpdateUserManagement().execute(
                        "http://$IP_ADDRESS/select_user.php",
                        "select * from 회원"
                    )
                }
                result.contains("실패") -> {

                }
                result == "empty" -> {
                    viewAdapter.clear()
                    viewAdapter.notifyDataSetChanged()
                }
                else -> {
                    try {
                        val jsonObject = JSONObject(result)
                        val jsonArray = jsonObject.getJSONArray("result")

                        viewAdapter.clear()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val userId = item.getString("id")
                            val userPw = item.getString("pw")
                            val userName = item.getString("name")

                            viewAdapter.addItem(UserInfo(userId, userPw, userName))
                            viewAdapter.notifyDataSetChanged()
                        }
                    } catch (e: JSONException) { }
                }
            }
        }
    }

    inner class DeleteUser : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val serverURL = p0[0]
            val userId = p0[1]
            val postParameters = "userid=$userId"

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

            UpdateUserManagement().execute(
                    "http://$IP_ADDRESS/select_user.php",
                    "select * from 회원"
            )
        }
    }

    inner class UpdateTest : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val serverURL = p0[0]
            val newId = p0[1]
            val newPw = p0[2]
            val newName = p0[3]
            val oldId = p0[4]
            val postParameters = "newid=$newId&newpw=$newPw&newname=$newName&oldid=$oldId"

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

            UpdateUserManagement().execute(
                "http://$IP_ADDRESS/select_user.php",
                "select * from 회원"
            )
        }
    }
}