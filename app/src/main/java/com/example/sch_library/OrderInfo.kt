package com.example.sch_library

class OrderInfo(
    var orderNumber: String,
    var orderDate: String,
    var orderTotalPrice: Int,
    var orderStatus: String,
    var cardInfo: CardInfo,
    var addressInfo: AddressInfo,
    var bookInfo: ArrayList<BookInfo> = ArrayList<BookInfo>(),
    var userId: String = ""
)

class PurchaseHistoryInfo(
    var orderTotalPrice: Int,
    var userId: String = ""
)