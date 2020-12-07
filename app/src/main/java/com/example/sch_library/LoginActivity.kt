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
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

//private const val IP_ADDRESS = "10.0.2.2"              // 에뮬레이터
const val IP_ADDRESS = "211.250.1.30:8888"   // 집
//const val IP_ADDRESS = "220.69.208.44:8888" // 연구실
//const val IP_ADDRESS = "210.204.197.234:8888"    // 카페
private const val TAG = "Login"

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

            val task = Login(applicationContext)
            task.execute("http://$IP_ADDRESS/login.php", id, pw)
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
}

class Login(private val context: Context) : AsyncTask<String, Void, String>() {
    override fun doInBackground(vararg p0: String?): String {
        val id = p0[1]
        val pw = p0[2]

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

        loginId.text.clear()
        loginPw.text.clear()

        var intent: Intent? = null

        loginPw.clearFocus()
        if (result == "admin") {
            intent = Intent(context, AdminActivity::class.java)
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else if (result == "") {
            Toast.makeText(context, "로그인 실패!", Toast.LENGTH_LONG).show()
        } else {
            try {
                val jsonObject = JSONObject(result)
                val jsonArray = jsonObject.getJSONArray("user")

                val item = jsonArray.getJSONObject(0)
                val id = item.getString("id")
                val pw = item.getString("pw")
                val name = item.getString("name")

                if (id != "" && pw != "") {
                    Toast.makeText(context, "로그인 성공!", Toast.LENGTH_LONG).show()

                    intent = Intent(context, UserActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("pw", pw)
                    intent.putExtra("name", name)
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            } catch (e: JSONException) {
                Log.d(TAG, "Login: ", e)
            }
        }
    }
}