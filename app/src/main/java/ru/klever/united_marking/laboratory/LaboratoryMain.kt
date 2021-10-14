package ru.klever.united_marking.laboratory

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.laboratory_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import ru.klever.united_marking.R
import ru.klever.united_marking.Reasons
import ru.klever.united_marking.Settings
import ru.klever.united_marking.TAG
import java.io.IOException

class LaboratoryMain:AppCompatActivity(), OnLaboratoryItemClickListener {
    private val REQUEST_CODE_SELECT_REASON=1

    private val myCodes= mutableListOf("123","456")
    private val sendInProgress=MutableLiveData(false)
    private val client = OkHttpClient()

    private lateinit var settings: Settings
    private lateinit var adapter: LaboratoryMainRecycleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.laboratory_main)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        settings = Settings(this)
        sendInProgress.observe(this, {
            if (it) {
                send_progressBar.visibility = View.VISIBLE
            } else {
                send_progressBar.visibility = View.INVISIBLE
            }
        })

        selected_reason.text = settings.laboratoryGetLastReason().reason_text
        group_reason_recycle.referencedIds.forEach { id ->
            findViewById<View>(id).setOnClickListener {
                val intent = Intent(this, LaboratoryReasonsRecycleView::class.java)
                startActivityForResult(intent, REQUEST_CODE_SELECT_REASON)
            }
        }

        send_button.setOnClickListener {
            val myJson = JSONObject(
                mapOf(
                    "deviceID" to settings.id,
                    "action" to "dropout",
                    "dropoutReason" to settings.laboratoryGetLastReason().reason_id,
                    "codes" to JSONArray(myCodes)
                )
            )
            sendCodes(this, myJson, sendInProgress)
        }

        val recyclerView: RecyclerView = findViewById(R.id.laboratory_main_recycleview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LaboratoryMainRecycleAdapter(myCodes, this)
        recyclerView.adapter = adapter


        data_code.setOnKeyListener { _, keyCode, keyEvent ->
            Log.d(TAG,keyCode.toString())
            if ((keyEvent.action==ACTION_UP && keyCode== KEYCODE_ENTER)) {
                val scanedCode=data_code.text.toString().trim()
                data_code.text?.clear()
                if (scanedCode !in myCodes) {
                    myCodes.add(scanedCode)
                    send_button.visibility=View.VISIBLE
                } else {
                    showAlert("Этот код уже был отсканирован")
                }
                adapter.notifyDataSetChanged()
                send_button.text="ОТПРАВИТЬ ${myCodes.size} КОДОВ"
                Log.d(TAG,myCodes.size.toString())

                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    private fun showAlert(message: String) {
        GlobalScope.launch {
            Handler(Looper.getMainLooper()).post {
                alert_icon.visibility=View.VISIBLE
                alert_textView.visibility=View.VISIBLE
                alert_textView.text=message

                val vibrator = this@LaboratoryMain.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(2000)
                }
                //val mediaPlayer=MediaPlayer.create(this@LaboratoryMain,R.raw.bike_horn)
                //mediaPlayer.start()
            }
            Thread.sleep(2000)
            Handler(Looper.getMainLooper()).post {
                alert_icon.visibility=View.INVISIBLE
                alert_textView.visibility=View.INVISIBLE
            }
        }
    }

    private fun clearMyCodes(){
        myCodes.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode==REQUEST_CODE_SELECT_REASON) {
            val resId = data!!.getStringExtra("reason_id")
            val resText= data.getStringExtra("reason_text")
            Log.d(ru.klever.united_marking.TAG, "$requestCode -- $resultCode -- $resId -- $resText ")
            super.onActivityResult(requestCode, resultCode, data)
            settings.laboratorySetReason(Reasons(resId,resText))
            selected_reason.text=resText
        }
    }

    override fun onItemClicked(code: String) {
        Log.d(ru.klever.united_marking.TAG,code)

    }

    fun sendCodes(context: Context, data:JSONObject, sendStatus:MutableLiveData<Boolean>) {
        sendStatus.postValue(true)
        val url=settings.getAPIUrl()
        Log.d(TAG,url)
        val mediaType="application/json; charset=utf-8".toMediaType()
        val requestBody=data.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .post(body = requestBody)
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
                        Log.d(TAG,"successful")
                        Handler(Looper.getMainLooper()).post {
                            clearMyCodes()
                        }
                    }
                }
                sendStatus.postValue(false)
            }
        })
    }
}



