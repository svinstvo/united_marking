package ru.klever.united_marking.add_km_to_reprint_pool

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
//import kotlinx.android.synthetic.main.add_code_main.*


import kotlinx.android.synthetic.main.add_km_to_reprint_pool.*
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
        setContentView(R.layout.add_km_to_reprint_pool)
        productByGtinCount.value=""
        settings= Settings(this)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                val textToPaste:String = clipboard.primaryClip?.getItemAt(0)?.text.toString().trim()
                try {
                    MainScope().launch {
                        sendCodes(this@AddKmToReprintPool,textToPaste)
                    }
                }catch (e: Exception) {
                    Toast.makeText(this,e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

        productByGtinCount.observe(this,{
            Log.d(TAG,it)
            if (it !=""){
                try {
                    //product_by_gtin_counter.text = it
                    val responseJson = JSONObject(it)
                    if (responseJson["status"].toString()=="ok") {
                        Log.d(TAG, responseJson["count"].toString())
                        product_by_gtin_counter.text=responseJson["count"].toString()
                    } else {
                        val intent = Intent(this, ErrorMessage::class.java)
                        intent.putExtra("message",responseJson["message"].toString())
                        startActivity(intent)
                    }

                } catch (e:Exception) {
                    Log.d(TAG,e.toString())
                    val intent = Intent(this, ErrorMessage::class.java)
                    intent.putExtra("message",it.toString())
                    startActivity(intent)
                }
            }

        })
    }


    private fun sendCodes(context: Context, data: String) {
        val myData= URLEncoder.encode(data)
        val url=settings.getAPIUrl()+"/print_km/add_km_to_reprint_pool?km=$myData&term_id=${settings.id}"
        Log.d(TAG,url)
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    productByGtinCount.postValue(e.localizedMessage.toString())
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Handler(Looper.getMainLooper()).post {
                            productByGtinCount.postValue(response.toString())
                        }
                    } else {
                        val responseBody=response.body?.string()
                        productByGtinCount.postValue(responseBody)
                        return
                    }
                }
            }
        })
    }

}