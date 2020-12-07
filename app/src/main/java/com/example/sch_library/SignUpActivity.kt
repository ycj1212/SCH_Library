package com.example.sch_library

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "SignUp";

private lateinit var inputId: EditText
private lateinit var inputPw: EditText
private lateinit var inputName: EditText

private lateinit var defaultHintColor: ColorStateList

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        inputId = findViewById(R.id.edittext_input_id)
        inputPw = findViewById(R.id.edittext_input_pw)
        inputName = findViewById(R.id.edittext_input_name)

        defaultHintColor = inputId.hintTextColors

        inputId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputId.setHintTextColor(defaultHintColor)
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }
        })

        inputPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputPw.setHintTextColor(defaultHintColor)
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }
        })

        inputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputName.setHintTextColor(defaultHintColor)
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }
        })

        val buttonSignUp: Button = findViewById(R.id.button_signup)
        buttonSignUp.setOnClickListener {
            // id와 pw가 올바르다면 DB에 저장
            val id = inputId.editableText.toString()
            val pw = inputPw.editableText.toString()
            val name = inputName.editableText.toString()

            val task = SignUp(applicationContext, this)
            task.execute("http://$IP_ADDRESS/signup.php", id, pw, name)
        }
    }

    class SignUp(private val context: Context, private val activity: Activity) : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val id = p0[1]
            val pw = p0[2]
            val name = p0[3]

            val serverURL = p0[0]
            val postParameters = "id=$id&pw=$pw&name=$name"

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

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (inputId.editableText.isBlank())     { inputId.setHintTextColor(Color.RED) }
            if (inputPw.editableText.isBlank())     { inputPw.setHintTextColor(Color.RED) }
            if (inputName.editableText.isBlank())   { inputName.setHintTextColor(Color.RED) }

            if (result?.contains("PRIMARY")!!) {
                inputId.text.clear()
                inputId.setHintTextColor(Color.RED)
            } else if (result == "회원가입 완료!") {
                Toast.makeText(context, result, Toast.LENGTH_LONG).show()

                activity.finish()
            }
        }
    }
}