package ru.klever.united_marking
data class ChekingResponse(var status:Boolean,var text:String)

open class CheckKM {
    fun checkKM(km:String):ChekingResponse {
        var resp=ChekingResponse(true,"")

        return resp
    }
}