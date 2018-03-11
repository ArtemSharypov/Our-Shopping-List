package com.artem.ourshoppinglist

interface PassBarcodeDataInterface {
    fun passBarcodeToActivity(barcode: String)
    fun getBarcodeFromActivity() : String
}