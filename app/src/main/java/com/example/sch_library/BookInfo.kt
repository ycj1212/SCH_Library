package com.example.sch_library

import java.io.Serializable

class BookInfo (
        var number: Int,
        var title: String,
        var stock: Int,
        var price: Int,
        var isSelected: Boolean = false,
        var count: Int = 1
) : Serializable