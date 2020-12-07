package com.example.sch_library.user

import android.app.AlertDialog
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
import com.example.sch_library.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class InfoManageFragment : Fragment() {
    lateinit var userNameInfo: EditText
    lateinit var userIdInfo: EditText
    lateinit var userPwInfo: EditText

    lateinit var noCardTextView: TextView

    lateinit var viewAdapter: CardAdapter

    lateinit var houseAddressInfo: AddressInfo
    lateinit var rectalAddressInfo: AddressInfo

    lateinit var houseZipCode: TextView
    lateinit var houseBasicAddress: TextView
    lateinit var houseDetailAddress: TextView
    lateinit var rectalZipCode: TextView
    lateinit var rectalBasicAddress: TextView
    lateinit var rectalDetailAddress: TextView

    override fun onResume() {
        super.onResume()

        UpdateUser().execute(
            "http://$IP_ADDRESS/select_user.php",
            "select * from 회원 where 아이디='$userId'",
            "select"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_info_manage, container, false)

        userNameInfo = view.findViewById(R.id.edittext_user_name)
        userIdInfo = view.findViewById(R.id.edittext_user_id)
        userPwInfo = view.findViewById(R.id.edittext_user_pw)

        UpdateUser().execute(
            "http://$IP_ADDRESS/select_user.php",
            "select * from 회원 where 아이디='$userId'",
            "select"
        )

        val updateCardButton: Button = view.findViewById(R.id.button_update_card)
        updateCardButton.setOnClickListener {
            CardListDialog().show()
        }

        val updateAddressButton: Button = view.findViewById(R.id.button_update_address)
        updateAddressButton.setOnClickListener {
            AddressInfoDialog().show()
        }

        val withdrawalButton: Button = view.findViewById(R.id.button_withdrawal)
        withdrawalButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setNegativeButton("아니요") { dialog, id ->
                dialog.dismiss()
            }
            builder.setTitle("회원 탈퇴").setMessage("정말 탈퇴 하실건가요?")
            builder.setPositiveButton("네") { dialog, id ->
                DeleteUser().execute(
                    "http://$IP_ADDRESS/delete_user.php",
                    userId
                )
                activity?.finish()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }

        val updateUserInfoButton: Button = view.findViewById(R.id.button_update_user_info)
        updateUserInfoButton.setOnClickListener {
            if (userIdInfo.text.toString() == userId) {
                UpdateUser().execute(
                    "http://$IP_ADDRESS/update_user.php",
                    "update 회원 set 아이디='${userIdInfo.text.toString()}', 비밀번호='${userPwInfo.text.toString()}', 이름='${userNameInfo.text.toString()}' where 아이디='$userId'",
                    "update"
                )
            } else {
                UpdateTest().execute(
                    "http://$IP_ADDRESS/test.php",
                    userIdInfo.text.toString(),
                    userPwInfo.text.toString(),
                    userNameInfo.text.toString(),
                    userId
                )
            }
        }

        return view
    }

    fun updateCard(serverUrl: String, query: String) {
        val temp = UpdateCard()
        temp.execute(serverUrl, query)
    }
    fun updateAddress(serverUrl: String, query: String) {
        val temp = UpdateAddress()
        temp.execute(serverUrl, query)
    }

    inner class CardListDialog : Dialog(context!!) {
        lateinit var recyclerView: RecyclerView
        lateinit var viewManager: RecyclerView.LayoutManager
        lateinit var addCardButton: Button

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_cardlist)

            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params

            viewManager = LinearLayoutManager(context)
            viewAdapter = CardAdapter()

            updateCard(
                "http://$IP_ADDRESS/select_card.php",
                "select * from 회원카드 where 아이디='$userId'"
            )

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

            noCardTextView = findViewById(R.id.textview_no_card_cardlist)

            addCardButton = findViewById(R.id.button_add_card_cardlist)
            addCardButton.setOnClickListener {
                val cardAddDialog = CardAddDialog()
                cardAddDialog.show()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class CardAdapter : RecyclerView.Adapter<CardAdapter.ViewHolder>() {
        private val items = ArrayList<CardInfo>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private var cardType: TextView = itemView.findViewById(R.id.textview_card_type)
            private var cardNumber: TextView = itemView.findViewById(R.id.textview_card_number)
            private var cardExpirationDate: TextView = itemView.findViewById(R.id.textview_card_expiration_date)

            init {
                itemView.setOnClickListener {
                    val cardUpdateDialog = CardUpdateDialog(items[adapterPosition])
                    cardUpdateDialog.show()
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

        fun addItem(item: CardInfo) { items.add(item) }
        fun cleanItems() { items.clear() }
    }

    inner class CardAddDialog : Dialog(context) {
        lateinit var cardType: EditText
        lateinit var cardNumber: EditText
        lateinit var cardExpirationDate: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_add_card)

            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params

            cardType = findViewById(R.id.edittext_card_type)
            cardNumber = findViewById(R.id.edittext_card_number)
            cardExpirationDate = findViewById(R.id.edittext_card_expiration_date)

            val cardAddButton: Button = findViewById(R.id.button_add_card)
            cardAddButton.setOnClickListener {
                val expirationDateTemp = cardExpirationDate.text.toString().split('/')
                val expirationYear = expirationDateTemp[1]
                val expirationMonth = expirationDateTemp[0].toInt()
                var expirationDate = "$expirationYear/$expirationMonth"

                if (expirationMonth == 1 or 3 or 5 or 7 or 8 or 10 or 12) {
                    expirationDate += "/31"
                } else if (expirationMonth == 4 or 6 or 9 or 11) {
                    expirationDate += "/30"
                } else {
                    expirationDate += "/28"
                }

                updateCard(
                    "http://$IP_ADDRESS/insert_card_address.php",
                    "insert into 회원카드 values ('$userId', ${cardNumber.text.toString()}, '$expirationDate', '${cardType.text.toString()}')"
                )
                dismiss()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class CardUpdateDialog(private val cardInfo: CardInfo) : Dialog(context) {
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
                val expirationDateTemp = cardExpirationDate.text.toString().split('/')
                val expirationYear = expirationDateTemp[1]
                val expirationMonth = expirationDateTemp[0].toInt()
                var expirationDate = "$expirationYear/$expirationMonth"

                if (expirationMonth == 1 or 3 or 5 or 7 or 8 or 10 or 12) {
                    expirationDate += "/31"
                } else if (expirationMonth == 4 or 6 or 9 or 11) {
                    expirationDate += "/30"
                } else {
                    expirationDate += "/28"
                }

                updateCard(
                    "http://$IP_ADDRESS/insert_card_address.php",
                    "update 회원카드 set 카드번호=${cardNumber.text.toString()}, 유효기간='$expirationDate', 카드종류='${cardType.text.toString()}' where 아이디='$userId' and 카드번호='${cardInfo.number}'"
                )
                dismiss()
            }

            val cardDeleteButton: Button = findViewById(R.id.button_delete_card_info)
            cardDeleteButton.setOnClickListener {
                updateCard(
                    "http://$IP_ADDRESS/insert_card_address.php",
                    "delete from 회원카드 where 카드번호=${cardNumber.text.toString()} and 아이디='$userId'"
                )
                dismiss()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class AddressInfoDialog : Dialog(context) {
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

            updateAddress(
                "http://$IP_ADDRESS/select_address.php",
                "select * from 회원주소 where 아이디='$userId'"
            )

            val houseAddress: LinearLayout = findViewById(R.id.layout_house_address)
            houseAddress.setOnClickListener {
                if (isEmptyHouseAddress()) {
                    AddressAddDialog(0).show()
                } else {
                    AddressUpdateDialog(0).show()
                }
            }

            val rectalAddress: LinearLayout = findViewById(R.id.layout_rectal_address)
            rectalAddress.setOnClickListener {
                if (isEmptyRectalAddress()) {
                    AddressAddDialog(1).show()
                } else {
                    AddressUpdateDialog(1).show()
                }
            }
        }

        private fun isEmptyHouseAddress(): Boolean = houseZipCode.text.isBlank() || houseBasicAddress.text.isBlank() || houseDetailAddress.text.isBlank()
        private fun isEmptyRectalAddress(): Boolean = rectalZipCode.text.isBlank() || rectalBasicAddress.text.isBlank() || rectalDetailAddress.text.isBlank()

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class AddressAddDialog(private val type: Int) : Dialog(context) {
        private lateinit var zipCode: EditText
        private lateinit var basicAddress: EditText
        private lateinit var detailAddress: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dialog_add_address)

            val params = window.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params

            zipCode = findViewById(R.id.edittext_zip_code)
            basicAddress = findViewById(R.id.edittext_basic_address)
            detailAddress = findViewById(R.id.edittext_detail_address)

            val addressAddButton: Button = findViewById(R.id.button_add_address)
            addressAddButton.setOnClickListener {
                if (type == 0) {
                    updateAddress(
                        "http://$IP_ADDRESS/insert_card_address.php",
                        "insert into 회원주소 (아이디, 배송지, 우편번호, 기본주소, 상세주소) values ('$userId', '자택', '${zipCode.text.toString()}', '${basicAddress.text.toString()}', '${detailAddress.text.toString()}')"
                    )
                } else {
                    updateAddress(
                        "http://$IP_ADDRESS/insert_card_address.php",
                        "insert into 회원주소 (아이디, 배송지, 우편번호, 기본주소, 상세주소) values ('$userId', '직장', '${zipCode.text.toString()}', '${basicAddress.text.toString()}', '${detailAddress.text.toString()}')"
                    )
                }
                dismiss()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class AddressUpdateDialog(private val type: Int) : Dialog(context) {
        private lateinit var zipCode: EditText
        private lateinit var basicAddress: EditText
        private lateinit var detailAddress: EditText

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

            if (type == 0) {
                zipCode.setText(houseAddressInfo.zipCode)
                basicAddress.setText(houseAddressInfo.basicAddress)
                detailAddress.setText(houseAddressInfo.detailAddress)
            } else {
                zipCode.setText(rectalAddressInfo.zipCode)
                basicAddress.setText(rectalAddressInfo.basicAddress)
                detailAddress.setText(rectalAddressInfo.detailAddress)
            }

            val addressUpdateButton: Button = findViewById(R.id.button_update_address)
            addressUpdateButton.setOnClickListener {
                if (type == 0) {
                    updateAddress(
                        "http://$IP_ADDRESS/insert_card_address.php",
                        "update 회원주소 set 우편번호='${zipCode.text.toString()}', 기본주소='${basicAddress.text.toString()}', 상세주소='${detailAddress.text.toString()}' where 아이디='$userId' and 배송지='자택'"
                    )
                } else {
                    updateAddress(
                        "http://$IP_ADDRESS/insert_card_address.php",
                        "update 회원주소 set 우편번호='${zipCode.text.toString()}', 기본주소='${basicAddress.text.toString()}', 상세주소='${detailAddress.text.toString()}' where 아이디='$userId' and 배송지='직장'"
                    )
                }
                dismiss()
            }
        }

        override fun onBackPressed() {
            dismiss()
        }
    }

    inner class UpdateCard : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val serverURL = p0[0]
            val query = p0[1]
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
                    updateCard(
                        "http://$IP_ADDRESS/select_card.php",
                        "select * from 회원카드 where 아이디='$userId'"
                    )
                }
                result.contains("실패") -> {

                }
                result == "empty" -> {
                    viewAdapter.cleanItems()
                    viewAdapter.notifyDataSetChanged()
                }
                else -> {
                    try {
                        val jsonObject = JSONObject(result)
                        val jsonArray = jsonObject.getJSONArray("result")
                        viewAdapter.cleanItems()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i);
                            val cardType = item.getString("cardtype")
                            val cardNumber = item.getString("cardnumber")
                            val cardExpirationDate = item.getString("cardexpirationdate")
                            val cardExpirationDateTemp = cardExpirationDate.split('-')
                            var cardExpirationYear = cardExpirationDateTemp[0].substring(2,4)
                            val expirationDate = "${cardExpirationDateTemp[1]}/$cardExpirationYear"

                            viewAdapter.addItem(CardInfo(cardType, cardNumber, expirationDate))
                        }

                        viewAdapter.notifyDataSetChanged()
                    } catch (e: JSONException) { }
                }
            }

            if (viewAdapter.itemCount == 0) {
                noCardTextView.visibility = View.VISIBLE
            } else {
                noCardTextView.visibility = View.GONE
            }
        }
    }

    inner class UpdateAddress : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val serverURL = p0[0]
            val query = p0[1]
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
                    updateAddress(
                        "http://$IP_ADDRESS/select_address.php",
                        "select * from 회원주소 where 아이디='$userId'"
                    )
                }
                result.contains("실패") -> {

                }
                result == "empty" -> {
                    houseZipCode.text = ""
                    houseBasicAddress.text = ""
                    houseDetailAddress.text = ""
                    rectalZipCode.text = ""
                    rectalBasicAddress.text = ""
                    rectalDetailAddress.text = ""
                }
                else -> {
                    try {
                        val jsonObject = JSONObject(result)
                        val jsonArray = jsonObject.getJSONArray("result")

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i);
                            val deliveryAddress = item.getString("delivery")
                            val zipCode = item.getString("zipcode")
                            val basicAddress = item.getString("basic")
                            val detailAddress = item.getString("detail")

                            if (deliveryAddress == "자택") {
                                houseAddressInfo = AddressInfo(zipCode, basicAddress, detailAddress)
                                houseZipCode.text = houseAddressInfo.zipCode
                                houseBasicAddress.text = houseAddressInfo.basicAddress
                                houseDetailAddress.text = houseAddressInfo.detailAddress
                            } else if (deliveryAddress == "직장") {
                                rectalAddressInfo = AddressInfo(zipCode, basicAddress, detailAddress)
                                rectalZipCode.text = rectalAddressInfo.zipCode
                                rectalBasicAddress.text = rectalAddressInfo.basicAddress
                                rectalDetailAddress.text = rectalAddressInfo.detailAddress
                            }
                        }
                    } catch (e: JSONException) { }
                }
            }
        }
    }

    inner class UpdateUser : AsyncTask<String, Void, String>() {
        private var type = ""
        override fun doInBackground(vararg p0: String?): String {
            val serverURL = p0[0]
            val query = p0[1]
            type = p0[2]!!
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
                    if (type == "update") {
                        userId = userIdInfo.text.toString()
                    }
                }
                result.contains("실패") -> {

                }
                result == "empty" -> {

                }
                else -> {
                    try {
                        val jsonObject = JSONObject(result)
                        val jsonArray = jsonObject.getJSONArray("result")

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val id = item.getString("id")
                            val pw = item.getString("pw")
                            val name = item.getString("name")

                            userIdInfo.setText(id)
                            userPwInfo.setText(pw)
                            userNameInfo.setText(name)
                        }
                    } catch (e: JSONException) { }
                }
            }
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

            Toast.makeText(context, result, Toast.LENGTH_LONG).show()

            if (result == "성공") {
                userId = userIdInfo.text.toString()
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

            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }
    }
}