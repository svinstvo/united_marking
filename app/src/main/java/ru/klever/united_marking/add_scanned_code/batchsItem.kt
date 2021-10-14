package ru.klever.united_marking.add_scanned_code

data class batchsItem(
    val batch_id: Int,
    val batch_print_name: String,
    val batch_status: Int,
    val order_id: Int,
    val product_id: Int,
    val product_name: String
)