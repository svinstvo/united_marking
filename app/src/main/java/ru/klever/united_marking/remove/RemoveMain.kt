package ru.klever.united_marking.remove

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
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

class RemoveMain:AppCompatActivity(), OnRemoveItemClickListener {
    private val REQUEST_CODE_SELECT_REASON=1

    private val myCodes= mutableListOf<String>()
    private val client = OkHttpClient()
    var sendResult=MutableLiveData<JSONObject>()

    private lateinit var settings: Settings
    private lateinit var adapter: RemoveMainRecycleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.remove_main)
        settings = Settings(this)

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        selected_reason.text = settings.removeGetLastReason().reason_text
        group_reason_recycle.referencedIds.forEach { id ->
            findViewById<View>(id).setOnClickListener {
                val intent = Intent(this, RemoveReasonsRecycleView::class.java)
                startActivityForResult(intent, REQUEST_CODE_SELECT_REASON)
            }
        }

        val recyclerView: RecyclerView = findViewById(R.id.dropout_main_recycleview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RemoveMainRecycleAdapter(myCodes, this)
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
                    fail_text="Неверная длинна"
                }
                if (km.get(24).hashCode()!=29){
                    success=false
                    fail_text="Нету символа GS"
                }

                if (success){
                    GlobalScope.launch {
                        val params= mapOf("term_id" to settings.id,"km" to km,"reason" to settings.removeGetLastReason().reason_id)
                        val responce= get(url=settings.getAPIUrl()+"remove_code", params = params)
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
            settings.removeSetReason(Reasons(resId,resText))
            Log.d(TAG,settings.removeGetLastReason().toString())
            selected_reason.text=resText
        }
    }

    override fun onItemClicked(code: String) {
        Log.d(ru.klever.united_marking.TAG,code)

    }



}



