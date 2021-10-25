package ru.klever.united_marking.add_scanned_code

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.add_scanned_code_main.*
import kotlinx.android.synthetic.main.add_scanned_code_select_batch_spinner_item.*
import kotlinx.android.synthetic.main.add_scanned_code_select_batch_spinner_item.view.*
import kotlinx.android.synthetic.main.code_viewer_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import ru.klever.united_marking.ErrorMessage
import ru.klever.united_marking.R
import ru.klever.united_marking.Settings
import ru.klever.united_marking.TAG
import java.io.IOException
import java.lang.Exception
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class AddCodeMain: AppCompatActivity(){
    private lateinit var settings: Settings
    private val client = OkHttpClient()
    private val sendInProgress= MutableLiveData(false)
        //private lateinit var batchsFromServer:MutableLiveData<String>
    private lateinit var batchs_array:MutableLiveData<Batchs>
    var sendResult=MutableLiveData<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_scanned_code_main)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        settings = Settings(this)
        var batchs=Batchs()
        batchs_array= MutableLiveData(Batchs())
        sendResult.value="ok"
        //editText_add_code.requestFocus()
        //add_code_textView.requestFocus()

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Set clipboard primary clip change listener
        clipboard.addPrimaryClipChangedListener {
            if (lifecycle.currentState ==Lifecycle.State.RESUMED) {
                val textToPaste:String = clipboard.primaryClip?.getItemAt(0)?.text.toString().trim()
                Log.d(TAG,textToPaste)

                try {
                    val selectedBatch:batchsItem= spinner_select_batch.selectedItem as batchsItem
                    val neededGTIN=selectedBatch.gtin.substring(1)
                    val scanedGTIN=textToPaste.substring(3,16)
                    Log.d(TAG,'"'+scanedGTIN+'"')
                    Log.d(TAG,'"'+neededGTIN+'"')

                    if(scanedGTIN != neededGTIN) {throw Exception("Ожидался код ${neededGTIN} а получили ${scanedGTIN} \n Не тот продукт")}
                } catch (e:Exception) {
                    sendResult.postValue(e.message)
                        //editText_add_code.text.clear()
                    return@addPrimaryClipChangedListener
                }

                try {

                    val selectedBatch:batchsItem= spinner_select_batch.selectedItem as batchsItem
                    sendCodes(this,textToPaste,selected_batch = selectedBatch,sendInProgress)
                    //editText_add_code.text.clear()

                }catch (e: Exception) {
                    Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show()
                }
            }
        }

//                    editText_add_code.setOnKeyListener { _, keyCode, keyEvent ->
//            editText_add_code.text.clear()
//                            if ((keyEvent.action== KeyEvent.ACTION_UP && keyCode== KeyEvent.KEYCODE_ENTER)) {
//                val scanedCode=editText_add_code.text.toString().trim()
//
//
//                                        try {
//                    val selectedBatch:batchsItem= spinner_select_batch.selectedItem as batchsItem
//                    sendCodes(this,scanedCode,selected_batch = selectedBatch,sendInProgress)
//                    editText_add_code.text.clear()
//                                                }catch (e: Exception) {
//                    Toast.makeText(this,e.toString(),Toast.LENGTH_LONG)
//
//                                                }
//
//
//                                    }
//                            return@setOnKeyListener false
//                            }



        val sdf=SimpleDateFormat("yyyy-MM-dd")
        val currentDate=sdf.format(Date()).toString()
        editText_Date.setText(currentDate)
        val context=this
        poehali.setOnClickListener {
            MainScope().launch {
                batchs_array.postValue(batchs.getDataFromServerByOrderName(context = context,editText_Date.text.toString()))
            }
        }

        MainScope().launch {
            batchs_array.postValue(batchs.getDataFromServerByOrderName(context = context,editText_Date.text.toString()))
        }

        batchs_array.observe(this, androidx.lifecycle.Observer {
            Log.d(TAG,it.toString())
            val adapter=AddCodeSpinerAdapter(this,it)
            spinner_select_batch.adapter=adapter
        })

        spinner_select_batch.onItemSelectedListener= object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //editText_add_code.requestFocus()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //editText_add_code.requestFocus()
            }
        }

        sendResult.observe(this,{
            Log.d(TAG,it)
            if (it != "ok") {
                val intent = Intent(this, ErrorMessage::class.java)
                intent.putExtra("message",it)
                startActivity(intent)
            }
        })
    }





    private fun sendCodes(context: Context, data: String, selected_batch:batchsItem, sendStatus: MutableLiveData<Boolean>) {
        val myData=URLEncoder.encode(data)
        val url=settings.getAPIUrl()+"/markirovka/km/add_code?km=$myData&term_id=${settings.id}&batch_id=${selected_batch.batch_id}"
        Log.d(TAG,url)
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    //Toast.makeText(context, "Ошибка сети \n ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    //sendStatus.postValue(false)
                    sendResult.postValue(e.localizedMessage)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Handler(Looper.getMainLooper()).post {
                            //Toast.makeText(context, "Ошибка сети \n $response", Toast.LENGTH_SHORT).show()
                            sendResult.postValue(response.toString())
                        }
                    } else {
                        sendResult.postValue(response.body?.string())
                        return
                    }
                }
            }
        })
    }
}