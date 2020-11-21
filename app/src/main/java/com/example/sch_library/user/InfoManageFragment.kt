package com.example.sch_library.user

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sch_library.AddressInfo
import com.example.sch_library.CardInfo
import com.example.sch_library.R

class InfoManageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info_manage, container, false)

        val updateCardButton: Button = view.findViewById(R.id.button_update_card)
        updateCardButton.setOnClickListener {
            CardListDialog(view).show()
        }

        val updateAddressButton: Button = view.findViewById(R.id.button_update_address)
        updateAddressButton.setOnClickListener {
            AddressInfoDialog(view).show()
        }

        return view
    }
}

class CardListDialog(private val view: View) : Dialog(view.context) {
    lateinit var recyclerView: RecyclerView
    lateinit var viewAdapter: CardAdapter
    lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_cardlist)

        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params

        viewManager = LinearLayoutManager(context)
        viewAdapter = CardAdapter(context)
        for (i in 1 until 5) {
            viewAdapter.addItem(CardInfo("BC카드", "$i$i$i$i$i$i$i$i$i$i$i$i$i$i$i$i", "05/22"))
        }

        recyclerView = findViewById(R.id.recyclerview_cardlist)
        recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }

    override fun onBackPressed() {
        dismiss()
    }
}

class CardAdapter(val context: Context) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {
    private val items = ArrayList<CardInfo>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var cardType: TextView = itemView.findViewById(R.id.textview_card_type)
        private var cardNumber: TextView = itemView.findViewById(R.id.textview_card_number)
        private var cardExpirationDate: TextView = itemView.findViewById(R.id.textview_card_expiration_date)

        init {
            itemView.setOnClickListener {
                CardUpdateDialog(context, items[adapterPosition]).show()
            }
        }

        fun setItem(item: CardInfo) {
            cardType.text = item.type
            cardNumber.text = item.number
            cardExpirationDate.text = item.expiration_date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.info_card, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: CardInfo) {
        items.add(item)
    }
}

class CardUpdateDialog(context: Context, private val cardInfo: CardInfo) : Dialog(context) {
    lateinit var cardType: EditText
    lateinit var cardNumber: EditText
    lateinit var cardExpirationDate: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update_card)

        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params

        cardType = findViewById(R.id.edittext_card_type)
        cardNumber = findViewById(R.id.edittext_card_number)
        cardExpirationDate = findViewById(R.id.edittext_card_expiration_date)

        cardType.setText(cardInfo.type)
        cardNumber.setText(cardInfo.number)
        cardExpirationDate.setText(cardInfo.expiration_date)

        val cardUpdateButton: Button = findViewById(R.id.button_update_card_info)
        cardUpdateButton.setOnClickListener {

        }

        val cardDeleteButton: Button = findViewById(R.id.button_delete_card_info)
        cardDeleteButton.setOnClickListener {

        }
    }

    override fun onBackPressed() {
        dismiss()
    }
}

class AddressInfoDialog(private val view: View) : Dialog(view.context) {
    lateinit var houseZipCode: TextView
    lateinit var houseBasicAddress: TextView
    lateinit var houseDetailAddress: TextView
    lateinit var rectalZipCode: TextView
    lateinit var rectalBasicAddress: TextView
    lateinit var rectalDetailAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_address_info)

        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params

        houseZipCode = findViewById(R.id.textview_house_zip_code)
        houseBasicAddress = findViewById(R.id.textview_house_basic_address)
        houseDetailAddress = findViewById(R.id.textview_house_detail_address)
        rectalZipCode = findViewById(R.id.textview_rectal_zip_code)
        rectalBasicAddress = findViewById(R.id.textview_rectal_basic_address)
        rectalDetailAddress = findViewById(R.id.textview_rectal_detail_address)

        val houseAddressInfo = AddressInfo("12345","경기도 평택시 팽성읍 안정순환로 32번길 35-4","상세주소 입니다.")
        val rectalAddressInfo = AddressInfo("54321","경기도 안성시 공도읍 공도3로 32-1","1층 OWL COFFEE")

        houseZipCode.text = houseAddressInfo.zipCode
        houseBasicAddress.text = houseAddressInfo.basicAddress
        houseDetailAddress.text = houseAddressInfo.detailAddress
        rectalZipCode.text = rectalAddressInfo.zipCode
        rectalBasicAddress.text = rectalAddressInfo.basicAddress
        rectalDetailAddress.text = rectalAddressInfo.detailAddress

        val houseAddress: LinearLayout = findViewById(R.id.layout_house_address)
        houseAddress.setOnClickListener {
            AddressUpdateDialog(context, houseAddressInfo).show()
        }

        val rectalAddress: LinearLayout = findViewById(R.id.layout_rectal_address)
        rectalAddress.setOnClickListener {
            AddressUpdateDialog(context, rectalAddressInfo).show()
        }
    }

    override fun onBackPressed() {
        dismiss()
    }
}

class AddressUpdateDialog(context: Context, private val addressInfo: AddressInfo) : Dialog(context) {
    lateinit var zipCode: EditText
    lateinit var basicAddress: EditText
    lateinit var detailAddress: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update_address)

        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = params

        zipCode = findViewById(R.id.edittext_zip_code)
        basicAddress = findViewById(R.id.edittext_basic_address)
        detailAddress = findViewById(R.id.edittext_detail_address)

        zipCode.setText(addressInfo.zipCode)
        basicAddress.setText(addressInfo.basicAddress)
        detailAddress.setText(addressInfo.detailAddress)

        val addressUpdateButton: Button = findViewById(R.id.button_update_address)
        addressUpdateButton.setOnClickListener {

        }
    }

    override fun onBackPressed() {
        dismiss()
    }
}