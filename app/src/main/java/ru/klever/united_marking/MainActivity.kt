package ru.klever.united_marking


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import ru.klever.united_marking.laboratory.LaboratoryMain

class MainActivity : AppCompatActivity() {
    val TAG="debuu"
      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)
          val settings:Settings= Settings(this)
          progressBar.visibility=View.INVISIBLE
          val loadstatus: MutableLiveData<Boolean> =MutableLiveData()


          loadstatus.observe(this, Observer { it ->
              if (it) {
                  progressBar.visibility = View.VISIBLE
                  load_settings_main.visibility=View.INVISIBLE
              } else {
                  progressBar.visibility = View.INVISIBLE
                  load_settings_main.visibility=View.VISIBLE
                  Log.d(TAG,settings.loadedfromserver.toString())
                  if (settings.loadedfromserver) {
                      startActivity(settings)
                  } else {
                      Log.d(TAG,"not loaded")
                      last_message.text="Не получилось получить настройки с сервера"
                  }
              }
          })





          load_settings_main.setOnClickListener {
              settings.loadSetingsMain(loadstatus)
          }

          load_settings_locale.setOnClickListener {
              startActivity(settings)
          }

          settings.loadSetingsMain(loadstatus)
    }

    private fun startActivity(settings: Settings) {
        when (settings.getRole()) {
            "laboratory" -> {
                val intent = Intent(this, LaboratoryMain::class.java)
                startActivity(intent)
            }
        }
    }
}