package ru.klever.united_marking.add_scanned_code

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import khttp.get
import kotlinx.android.synthetic.main.add_scanned_code_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import ru.klever.united_marking.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AddCodeMain: AppCompatActivity() {
    private lateinit var settings: Settings
    private val client = OkHttpClient()
    var sendResult=MutableLiveData<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.add_scanned_code_main)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        settings = Settings(this)
        val cal: Calendar = GregorianCalendar()
        cal.add(Calendar.DATE, 1)
        add_km_batch_date.text = SimpleDateFormat("yyyy-MM-dd").format(cal.getTime())

        day_plus_1.setOnClickListener() {
            cal.add(Calendar.DATE, 1)
            add_km_batch_date.text = SimpleDateFormat("yyyy-MM-dd").format(cal.getTime())
        }
        day_minus_1.setOnClickListener() {
            cal.add(Calendar.DATE, -1)
            add_km_batch_date.text = SimpleDateFormat("yyyy-MM-dd").format(cal.getTime())
        }

        sendResult.observe(this) {
            if (it["status"]!="ok") {
                val intent = Intent(this, ErrorMessage::class.java)
                intent.putExtra("message",it["text"].toString())
                startActivity(intent)
            }
        }

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
                    GlobalScope.launch {
                        val params= mapOf("term_id" to settings.id,"batch_date" to SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()),"km" to km)
                        val responce= get(url=settings.getAPIUrl()+"add_km", params = params)
                        try {
                            sendResult.postValue(responce.jsonObject)
                        } catch (e:Exception) {
                            Log.d(TAG,e.toString())
                        }
                    }
                }else{
                    val intent = Intent(this, ErrorMessage::class.java)
                    intent.putExtra("message",fail_text)
                    startActivity(intent)
                }
            }
        }
    }
}