package com.example.sch_library

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    lateinit var inputId: EditText
    lateinit var inputPw: EditText
    lateinit var inputName: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        inputId = findViewById(R.id.edittext_input_id)
        inputPw = findViewById(R.id.edittext_input_pw)
        inputName = findViewById(R.id.edittext_input_name)

        val buttonSignUp: Button = findViewById(R.id.button_signup)
        buttonSignUp.setOnClickListener {
            // id와 pw가 올바르다면 DB에 저장

            finish()
        }
    }
}