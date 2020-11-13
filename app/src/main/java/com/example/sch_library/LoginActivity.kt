package com.example.sch_library

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    lateinit var loginId: EditText
    lateinit var loginPw: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginId = findViewById(R.id.edittext_id)
        loginPw = findViewById(R.id.edittext_pw)

        val buttonLogin: Button = findViewById(R.id.button_login)
        buttonLogin.setOnClickListener {
            val id = loginId.editableText.toString()
            val pw = loginPw.editableText.toString()
<<<<<<< HEAD
            var intent: Intent? = null
            // DB 검증
            if (id == "admin") {     // 관리자
                intent = Intent(this, AdminActivity::class.java)
            } else {    // 회원
                println("여기")
                intent = Intent(this, UserActivity::class.java)
=======
            // DB 검증
            if (id == "admin") {     // 관리자
                val intent = Intent(this, AdminActivity::class.java)
            } else {    // 회원
                val intent = Intent(this, UserActivity::class.java)
>>>>>>> 72ce08b77bda28f29da7a4371941cf529d43e961
                intent.putExtra("id", id)
                intent.putExtra("pw", pw)
            }
            startActivity(intent)
        }

        val buttonGoSignUp: Button = findViewById(R.id.button_go_signup)
        buttonGoSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
<<<<<<< HEAD
}

=======
}
>>>>>>> 72ce08b77bda28f29da7a4371941cf529d43e961