package ru.klever.united_marking.dropout

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
import kotlinx.android.synthetic.main.dropout_main.*
import okhttp3.*
import ru.klever.united_marking.*
import java.io.IOException
import java.net.URLEncoder

class DropoutMain:AppCompatActivity(), OnDropoutItemClickListener {
    private val REQUEST_CODE_SELECT_REASON=1

    private val myCodes= mutableListOf("123","456")
    private val client = OkHttpClient()
    var sendCodesResult=MutableLiveData<String>()

    private lateinit var settings: Settings
    private lateinit var adapter: DropoutMainRecycleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dropout_main)
        settings = Settings(this)
        sendCodesResult.value="ok"
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


        clipboard.addPrimaryClipChangedListener {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                val textToPaste:String = clipboard.primaryClip?.getItemAt(0)?.text.toString().trim()
                sendCodes(this,textToPaste,settings.dropoutGetLastReason().reason_id)

            }
        }
        sendCodesResult.observe(this,{
            Log.d(TAG,it.toString())
            if (it != "ok") {
                val intent = Intent(this, ErrorMessage::class.java)
                intent.putExtra("message",it.toString())
                startActivity(intent)
            }
        })

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

    private fun sendCodes(context: Context, data: String,reason:String) {
        val myData= URLEncoder.encode(data)
        val url=settings.getAPIUrl()+"/markirovka/km/dropout_code?km=$myData&term_id=${settings.id}&reason=${reason}"
        Log.d(TAG,url)
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    //Toast.makeText(context, "Ошибка сети \n ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    sendCodesResult.postValue(e.localizedMessage)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Handler(Looper.getMainLooper()).post {
                            //Toast.makeText(context, "Ошибка сети \n $response", Toast.LENGTH_SHORT).show()
                            sendCodesResult.postValue(response.toString())
                        }
                    } else {
                        val responseBody=response.body?.string()
                        sendCodesResult.postValue(responseBody)
                        return
                    }
                }
            }
        })
    }

}



