package com.example.sch_library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class LogoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_logout, container, false)

        val logoutButton: Button = view.findViewById(R.id.button_logout)
        logoutButton.setOnClickListener {
            Toast.makeText(context, "로그아웃 완료!", Toast.LENGTH_LONG).show()
            activity?.finish()
        }

        return view
    }
}