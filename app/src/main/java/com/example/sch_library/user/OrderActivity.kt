package com.example.sch_library.user

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.BookInfo
import com.example.sch_library.R
import com.example.sch_library.ViewAdapter

class OrderActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: ViewAdapter
    lateinit var viewManager: RecyclerView.LayoutManager

    lateinit var zipCode: EditText
    lateinit var basicAddress: EditText
    lateinit var detailAddress: EditText

    lateinit var cardType: EditText
    lateinit var cardNumber: EditText
    lateinit var cardExpirationDate: EditText

    lateinit var addressRadioGroup: RadioGroup
    lateinit var homeRadioButton: RadioButton
    lateinit var rectalRadioButton: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        viewManager = LinearLayoutManager(applicationContext)
        viewAdapter = ViewAdapter(1)

        for (i in 1 until 50) {
            viewAdapter.addItem(BookInfo(i, "책$i", i, i))
        }

        recyclerView = findViewById(R.id.recyclerview_order)
        recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        zipCode = findViewById(R.id.edittext_zip_code)
        basicAddress = findViewById(R.id.edittext_basic_address)
        detailAddress = findViewById(R.id.edittext_detail_address)
        cardType = findViewById(R.id.edittext_card_type)
        cardNumber = findViewById(R.id.edittext_card_number)
        cardExpirationDate = findViewById(R.id.edittext_card_expiration_date)

        addressRadioGroup = findViewById(R.id.radiogroup_address)
        homeRadioButton = findViewById(R.id.radiobutton_home)
        rectalRadioButton = findViewById(R.id.radiobutton_rectal)

        addressRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.radiobutton_home -> {

                }
                R.id.radiobutton_rectal -> {

                }
            }
        }

        val updateAddressButton: Button = findViewById(R.id.button_update_address)
        updateAddressButton.setOnClickListener {

        }

        val updateCardButton: Button = findViewById(R.id.button_update_card)
        updateCardButton.setOnClickListener {

        }

        val addCardButton: Button = findViewById(R.id.button_add_card)
        addCardButton.setOnClickListener {

        }
    }
}