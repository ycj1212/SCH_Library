package com.example.sch_library

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sch_library.admin.AdminActivity
import com.example.sch_library.user.UserActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

private const val IP_ADDRESS = "IP 주소";
private const val TAG = "phptest";

private lateinit var loginId: EditText
private lateinit var loginPw: EditText

class LoginActivity : AppCompatActivity() {
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginId = findViewById(R.id.edittext_id)
        loginPw = findViewById(R.id.edittext_pw)

        val buttonLogin: Button = findViewById(R.id.button_login)
        buttonLogin.setOnClickListener {
            val id = loginId.editableText.toString()
            val pw = loginPw.editableText.toString()

            val task = ConnectAccount(applicationContext)
            task.execute("http://$IP_ADDRESS/condb.php", id, pw)
        }

        val buttonGoSignUp: Button = findViewById(R.id.button_go_signup)
        buttonGoSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        if (intervalTime in 0..FINISH_INTERVAL_TIME) {
            super.onBackPressed()
            finish()
        } else {
            backPressedTime = tempTime
            Toast.makeText(this, "뒤로가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    class ConnectAccount(private val context: Context) : AsyncTask<String, Void, String>() {
        private var id: String? = null
        private var pw: String? = null

        override fun doInBackground(vararg p0: String?): String {
            id = p0[1]
            pw = p0[2]

            val serverURL = p0[0]
            val postParameters = "id=$id&pw=$pw"

            try {
                val url = URL(serverURL)
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.apply {
                    readTimeout = 5000
                    connectTimeout = 5000
                    requestMethod = "POST"
                    connect()
                }

                val outputStream = httpURLConnection.outputStream.apply {
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

            loginId.text.clear()
            loginPw.text.clear()

            Toast.makeText(context, result, Toast.LENGTH_LONG).show()

            if (result == "") {
                var intent: Intent? = null
                // DB 검증
                if (id == "admin") {     // 관리자
                    intent = Intent(context, AdminActivity::class.java)
                } else {    // 회원
                    intent = Intent(context, UserActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("pw", pw)
                }
                context.startActivity(intent)
            }
        }
    }

    class InsertData : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }

        override fun doInBackground(vararg p0: String?): String {
            val name = p0[1]
            val country = p0[2]

            val serverURL = p0[0]
            val postParameters = "name=$name&country=$country"

            try {
                val url = URL(serverURL)
                val httpURLConnection = url.openConnection() as HttpURLConnection

                httpURLConnection.readTimeout = 5000
                httpURLConnection.connectTimeout = 5000
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.connect()

                val outputStream = httpURLConnection.outputStream
                outputStream.write(postParameters.toByteArray())
                outputStream.flush()
                outputStream.close()

                val responseStatusCode = httpURLConnection.responseCode
                Log.d(TAG, "POST response code - $responseStatusCode")

                val inputStream = if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.inputStream
                } else {
                    httpURLConnection.errorStream
                }

                val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
                val bufferedReader = BufferedReader(inputStreamReader)

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
                Log.d(TAG, "InsertData: Error ", e)
                return "Error: ${e.message}"
            }
        }
    }
}

