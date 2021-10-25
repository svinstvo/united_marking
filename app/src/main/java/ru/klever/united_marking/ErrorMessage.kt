package ru.klever.united_marking

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.error_message.*
import android.media.RingtoneManager

import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
//import okhttp3.*
import java.lang.Exception
import khttp.post





class ErrorMessage :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_message)
        val intent=intent
        val errorMessage=intent.getStringExtra("message")!!
        error_message_text.text=errorMessage
        val settings= Settings(this)
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            val pattern = longArrayOf(1500, 800, 800, 800)
            //vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            vibrator.vibrate(VibrationEffect.createWaveform(pattern,0))
        } else {
            vibrator.vibrate(1500)
        }
        error_message_close_error_button.setOnClickListener {
            finish()
        }
        GlobalScope.launch { sendLogs(errorMessage,settings.getIP(),settings.id) }

    }
    private fun sendLogs(errorMessage:String,ip: String,id:String){
        try{
            val message = mapOf("message" to errorMessage,"client_id" to id,"ip" to ip)
            val r = post(url = "http://nginx.klever.ru/rest/tsd/logs", json= message)
        } catch (e:Exception) {
            Log.d(TAG,e.toString())
        }
    }

}