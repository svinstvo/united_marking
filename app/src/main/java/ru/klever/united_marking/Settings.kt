package ru.klever.united_marking
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import android.net.wifi.WifiManager
import java.net.InetAddress


const val TAG="debuu"

class Settings (_context: Context, _ip:String="", _id:String="", _settingsUrl:String=""): ViewModel(){
    var ip:String=_ip
    var id:String=_id
    var settingsUrl:String=_settingsUrl

    val context=_context
    var loadedfromserver:Boolean=false

    private val prefName="FILE_PREF_XML"
    private val sharedpref: SharedPreferences = context.getSharedPreferences(prefName, AppCompatActivity.MODE_PRIVATE)
    private val client = OkHttpClient()

    //try to read local preference
    init {
        Log.d(TAG,"start init")
        try {
            id= sharedpref.getString("id", "").toString()
            } catch (e:Exception){

            }
        //if id empty generate new and save
        if (id=="") {
            val edit=sharedpref.edit()

            id = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                //val formatted = current.format(formatter)
                current.format(formatter)
            } else {
                SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())

            }
            edit.putString("id",id)
            edit.apply()
        }
        // try read settings url from stored json
        try {
            settingsUrl=JSONObject(sharedpref.getString("settingsJSON", "")).getString("settingsUrl")
        } catch (e:Exception){
        }
        // if settings url empty set default url
        if (settingsUrl==""){
            settingsUrl="http://nginx.klever.ru/terminal/settings"
        }
    }


    private fun load_settings_locale() {
        Log.d(TAG,"load settings")
    }


    private fun save_settings_locale(settingsJSON:String){
        val editor=sharedpref.edit()
        editor.putString("settingsJSON",settingsJSON)
        editor.apply()
    }


    private fun load_settings_from_net():String{
        val spec= "$settingsUrl?id=$id"
        Log.d(TAG,spec)
        val settingsJSON= URL(spec).readText()
        return settingsJSON
    }


    fun loadSetingsMain(loadstatus:MutableLiveData<Boolean>){
        loadstatus.postValue(true)
        val spec= "$settingsUrl?id=$id"
        Log.d(TAG,spec)
        run(spec,loadstatus)
    }

    fun getRoles(): JSONArray {
        val jsonObject= JSONObject(sharedpref.getString("settingsJSON", "")).getString("roles")
        return JSONArray(jsonObject)
    }

    fun getUpdatePath():String{
        val updatePath= JSONObject(sharedpref.getString("settingsJSON", "")).getString("update_path")
        return updatePath
    }
    fun getIP(): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val intIPAddress= wifiManager.connectionInfo.ipAddress
        val ip = InetAddress.getByAddress(intToByteArray(intIPAddress))
        return ip.hostAddress
    }
    fun getCustomKey(key:String):String{
        return JSONObject(sharedpref.getString("settingsJSON", "")).getString(key)

    }

    fun dropoutSetReason(reason:Reasons){
        val editor=sharedpref.edit()
        editor.putString("dropout_last_reason_text",reason.reason_text)
        editor.putString("dropout_last_reason_id",reason.reason_id)
        editor.apply()
    }



    fun dropoutGetLastReason():Reasons{
        val reasonText:String = sharedpref.getString("dropout_last_reason_text","Выберите причину")!!
        val reasonId:String = sharedpref.getString("dropout_last_reason_id","")!!
        return Reasons(reasonId,reasonText)
    }

    fun dropoutGetAllReasonsAndId(): MutableList<Reasons> {
        val json=JSONObject(sharedpref.getString("settingsJSON", "")).getString("available_dropout_reasons")
        val availableReasons=JSONObject(json)
        val list= mutableListOf<Reasons>()

        for (i in availableReasons.keys()){
            val curentReason=Reasons(i,availableReasons[i].toString())
            list.add(curentReason).toString()
        }
        return list
    }

    fun removeSetReason(reason:Reasons){
        val editor=sharedpref.edit()
        editor.putString("remove_last_reason_text",reason.reason_text)
        editor.putString("remove_last_reason_id",reason.reason_id)
        editor.apply()
    }
    fun removeGetLastReason():Reasons{
        val reasonText:String = sharedpref.getString("remove_last_reason_text","Выберите причину")!!
        val reasonId:String = sharedpref.getString("remove_last_reason_id","")!!
        return Reasons(reasonId,reasonText)
    }

    fun removeGetAllReasonsAndId(): MutableList<Reasons> {
        val json=JSONObject(sharedpref.getString("settingsJSON", "")).getString("available_remove_reasons")
        val availableReasons=JSONObject(json)
        val list= mutableListOf<Reasons>()

        for (i in availableReasons.keys()){
            val curentReason=Reasons(i,availableReasons[i].toString())
            list.add(curentReason).toString()
        }
        return list
    }
    fun getAPIUrl():String {
        return JSONObject(sharedpref.getString("settingsJSON", "")).getString("rest_apiURL")
    }

    fun getDepartment():String{
        //return sharedpref.getString("department","")!!
        return JSONObject(sharedpref.getString("settingsJSON", "")).getString("department")
    }

    private fun intToByteArray(data: Int):ByteArray {
        val buffer:ByteArray= ByteArray(4)
        buffer[0] = (data shr 0).toByte()
        buffer[1] = (data shr 8).toByte()
        buffer[2] = (data shr 16).toByte()
        buffer[3] = (data shr 24).toByte()
        return buffer
    }

    private fun run(spec:String, loadStatus:MutableLiveData<Boolean>) {
        val request = Request.Builder()
            .url(spec)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Ошибка сети \n ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    loadStatus.postValue(false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Ошибка сети \n $response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        save_settings_locale(response.body!!.string())
                        loadedfromserver=true
                    }
                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }
                }
                loadStatus.postValue(false)
            }
        })
    }
}
