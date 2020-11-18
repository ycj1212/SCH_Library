package com.example.sch_library

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sch_library.admin.AdminActivity
import com.example.sch_library.user.UserActivity

class LoginActivity : AppCompatActivity() {
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

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
            var intent: Intent? = null
            // DB 검증
            if (id != "admin") {     // 관리자
                intent = Intent(this, AdminActivity::class.java)
            } else {    // 회원
                println("여기")
                intent = Intent(this, UserActivity::class.java)
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

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        if (intervalTime in 0..FINISH_INTERVAL_TIME) {
            super.onBackPressed()
        } else {
            backPressedTime = tempTime
            Toast.makeText(this, "뒤로가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
}

