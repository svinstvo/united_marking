package ru.klever.united_marking.dropout

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import khttp.get
import kotlinx.android.synthetic.main.dropout_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import ru.klever.united_marking.*
import java.io.IOException
import java.lang.Exception
import java.net.URLEncoder
import java.text.SimpleDateFormat

class DropoutMain:AppCompatActivity(), OnDropoutItemClickListener {
    private val REQUEST_CODE_SELECT_REASON=1

    private val myCodes= mutableListOf<String>()
    private val client = OkHttpClient()
    var sendResult=MutableLiveData<JSONObject>()

    private lateinit var settings: Settings
    private lateinit var adapter: DropoutMainRecycleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.dropout_main)
        settings = Settings(this)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        selected_reason.text = settings.dropoutGetLastReason().reason_text
        group_reason_recycle.referencedIds.forEach { id ->
            findViewById<View>(id).setOnClickListener {
                val intent = Intent(this, DropoutReasonsRecycleView::class.java)
                startActivityForResult(intent, REQUEST_CODE_SELECT_REASON)
            }
        }

        val recyclerView: RecyclerView = findViewById(R.id.dropout_main_recycleview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DropoutMainRecycleAdapter(myCodes, this)
        recyclerView.adapter = adapter

        sendResult.observe(this){
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
                        val params= mapOf("term_id" to settings.id,"department" to settings.getDepartment(), "km" to km,"reason" to settings.dropoutGetLastReason().reason_id)
                        val responce= get(url=settings.getAPIUrl()+"dropout_code", params = params)
                        try {
                            sendResult.postValue(responce.jsonObject)
                        } catch (e: Exception) {
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode==REQUEST_CODE_SELECT_REASON) {
            val resId = data!!.getStringExtra("reason_id")
            val resText= data!!.getStringExtra("reason_text")
            Log.d(ru.klever.united_marking.TAG, "$requestCode -- $resultCode -- $resId -- $resText ")
            super.onActivityResult(requestCode, resultCode, data)
            settings.dropoutSetReason(Reasons(resId,resText))
            selected_reason.text=resText
        }
    }

    override fun onItemClicked(code: String) {
        Log.d(ru.klever.united_marking.TAG,code)

    }



}



