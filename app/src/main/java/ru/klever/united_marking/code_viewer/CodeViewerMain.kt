package ru.klever.united_marking.code_viewer

import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.android.synthetic.main.code_viewer_main.*

import okhttp3.*
import ru.klever.united_marking.R
import ru.klever.united_marking.Settings
import ru.klever.united_marking.TAG
import java.io.IOException


class CodeViewerMain: AppCompatActivity(){
    private lateinit var settings: Settings
    private val client = OkHttpClient()
    private val sendInProgress=MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.code_viewer_main)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        settings = Settings(this)
        scan_field.requestFocus()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Set clipboard primary clip change listener
        clipboard.addPrimaryClipChangedListener {
            if (lifecycle.currentState==Lifecycle.State.RESUMED){
                val scanedCode: String = clipboard.primaryClip?.getItemAt(0)?.text.toString().trim()
                //val scanedCode=scan_field.text.toString().trim()
                Log.d(TAG,scanedCode)
                sendCodes(this,scanedCode,sendInProgress)
                last_scanned_code.text=scan_field.text
                scan_field.text.clear()
            }
        }
//        scan_field.setOnKeyListener { _, keyCode, keyEvent ->
//            Log.d(TAG,keyCode.toString())
//            if ((keyEvent.action== KeyEvent.ACTION_UP && keyCode== KeyEvent.KEYCODE_ENTER)) {
//                val scanedCode=scan_field.text.toString().trim()
//                Log.d(TAG,scanedCode)
//                sendCodes(this,scanedCode,sendInProgress)
//                last_scanned_code.text=scan_field.text
//                scan_field.text.clear()
//            }
//            return@setOnKeyListener false
//        }
    }

    private fun sendCodes(context: Context, data: String, sendStatus: MutableLiveData<Boolean>) {
        sendStatus.postValue(true)
        val url=settings.getAPIUrl()+"/markirovka/km/show_code_info?km=$data"
        Log.d(TAG,url)
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Ошибка сети \n ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    sendStatus.postValue(false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Ошибка сети \n $response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d(TAG,response.headers.toString())
                        val bodyResponce=response.body!!.string()
                        val gson=Gson()
                        val productPage= gson.fromJson(bodyResponce,ProductPage::class.java)
                        Log.d(TAG,productPage.name)
                        Handler(Looper.getMainLooper()).post {
                            product_name.text=productPage.name
                            if (productPage.image != null) {
                                val decodedString=Base64.decode(productPage.image,Base64.DEFAULT)
                                val bitmap=BitmapFactory.decodeByteArray(decodedString,0,decodedString.size)
                                product_image.setImageBitmap(bitmap)
                            }


                        }
                    }
                }
                sendStatus.postValue(false)
            }
        })
    }
}