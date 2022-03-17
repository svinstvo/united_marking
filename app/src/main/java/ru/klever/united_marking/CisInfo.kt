package ru.klever.united_marking.code_viewer

data class cisInfo(
    val brand: String,
    val cis: String,
    val emissionDate: String,
    val emissionType: String,
    val expirationDate: String,
    val gtin: String,
    val markWithdraw: Boolean,
    val ownerInn: String,
    val ownerName: String,
    val packageType: String,
    val prVetDocument: String,
    val producedDate: String,
    val producerInn: String,
    val producerName: String,
    val productGroup: String,
    val productGroupId: Int,
    val productName: String,
    val requestedCis: String,
    val status: String,
    val statusEx: String,
    val tnVedEaes: String,
    val tnVedEaesGroup: String
)