package ru.klever.united_marking.add_km_to_reprint_pool

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import khttp.get
//import kotlinx.android.synthetic.main.add_code_main.*


import kotlinx.android.synthetic.main.add_km_to_reprint_pool.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import ru.klever.united_marking.ErrorMessage
import ru.klever.united_marking.R
import ru.klever.united_marking.Settings
import ru.klever.united_marking.TAG
import java.io.IOException
import java.lang.Exception
import java.net.URLEncoder

class AddKmToReprintPool: AppCompatActivity() {
    var productByGtinCount=MutableLiveData<String>()
    lateinit var settings: Settings
    private val client = OkHttpClient()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.add_km_to_reprint_pool)
        productByGtinCount.value=""
        settings= Settings(this)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.addPrimaryClipChangedListener {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                val km: String = clipboard.primaryClip?.getItemAt(0)?.text.toString().trim()

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
                    add_km_to_reprint_pool_spiner.visibility= View.VISIBLE
                    GlobalScope.launch {
                        val params= mapOf("term_id" to settings.id,"km" to km)
                        try {
                            val responce= get(url=settings.getAPIUrl()+"add_km_to_reprint_pool", params = params)
                            productByGtinCount.postValue(responce.text)

                        } catch (e: Exception) {
                            Log.d(TAG,e.toString())
                            productByGtinCount.postValue(e.toString())
                        }
                    }
                }else{
                    val intent = Intent(this, ErrorMessage::class.java)
                    intent.putExtra("message",fail_text)
                    startActivity(intent)
                }
            }
        }

        productByGtinCount.observe(this) {
            add_km_to_reprint_pool_spiner.visibility=View.INVISIBLE
            Log.d(TAG, it)
            product_by_gtin_counter.text=it
        }
    }
}