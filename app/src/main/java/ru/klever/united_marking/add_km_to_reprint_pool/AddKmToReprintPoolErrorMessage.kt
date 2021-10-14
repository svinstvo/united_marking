package ru.klever.united_marking.add_km_to_reprint_pool

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.add_km_to_reprint_pool_error_message.*
import ru.klever.united_marking.R
import android.media.RingtoneManager

import android.media.Ringtone
import android.net.Uri
import java.lang.Exception


class AddKmToReprintPoolErrorMessage :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_km_to_reprint_pool_error_message)
        val intent=intent
        add_km_to_reprint_pool_error_message.text=intent.getStringExtra("message")

        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        add_km_to_reprint_pool_close_error_button.setOnClickListener {
            finish()
        }
    }
}