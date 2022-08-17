package ru.klever.united_marking.code_viewer

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import khttp.get
import kotlinx.android.synthetic.main.code_viewer_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import ru.klever.united_marking.ErrorMessage
import ru.klever.united_marking.R
import ru.klever.united_marking.Settings
import ru.klever.united_marking.TAG


class CodeViewerMain: AppCompatActivity(){
    private lateinit var settings: Settings
    private val server_response=MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.code_viewer_main)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        settings = Settings(this)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        server_response.observe(this) {
            if (it == "sending") {
                info_loading_spinner.visibility = View.VISIBLE
            }else if (it=="") {
                info_loading_spinner.visibility=View.INVISIBLE
            } else {
                info_loading_spinner.visibility=View.INVISIBLE
                try {
                    val jsonArray = JSONTokener(it).nextValue() as JSONArray
                    if (jsonArray.getJSONObject(0).has("errorCode")) {
                        val intent = Intent(this, ErrorMessage::class.java)
                        intent.putExtra("message","Ответ ЦРПТ: "+jsonArray.getJSONObject(0).getString("errorMessage"))
                        startActivity(intent)
                    } else{
                        val cisInfo: JSONObject =jsonArray.getJSONObject(0).getJSONObject("cisInfo")
                        info_product_name.text=getValue(cisInfo,"productName")
                        info_emission_date.text=getValue(cisInfo,"emissionDate")
                        info_emission_type.text=getValue(cisInfo,"emissionType")
                        info_produced_date.text=getValue(cisInfo,"producedDate")
                        info_expiration_date.text=getValue(cisInfo,"expirationDate")
                        info_status.text=getValue(cisInfo,"status")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }
            }
        }

        clipboard.addPrimaryClipChangedListener {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                val km: String = clipboard.primaryClip?.getItemAt(0)?.text.toString().trim()
                Log.d(TAG,km)
                var success = true
                var fail_text=""

                if (km.length!=31){
                    success=false
                    fail_text="Неверная длина кода $km"
                } else {
                    if (km[24].hashCode()!=29 ){
                        success=false
                        fail_text="Нету символа GS $km"
                    }
                }

                if (success){
                    info_product_name.text=""
                    info_emission_date.text=""
                    info_emission_type.text=""
                    info_produced_date.text=""
                    info_expiration_date.text=""
                    info_status.text=""
                    GlobalScope.launch {
                        server_response.postValue("sending")
                        val params= mapOf("km" to km)
                        try {
                        val responce= get(url=settings.getAPIUrl()+"show_info_about_km", params = params)
                            server_response.postValue(responce.text)
                            Log.d(TAG,settings.getAPIUrl()+"show_info_about_km")
                        } catch (e: Exception) {
                            Log.d(TAG,e.toString())
                            server_response.postValue("")
                        }
                    }
                }else{
                    Log.d(TAG,km)
                    val intent = Intent(this, ErrorMessage::class.java)
                    intent.putExtra("message",fail_text)
                    startActivity(intent)
                }
            }
        }
    }

    private fun getValue(cisInfo: JSONObject, field: String): String {
        var resp=""
        resp = try {
            cisInfo.getString(field)
        } catch (e: java.lang.Exception) {
            ""
        }
        return resp
    }
}