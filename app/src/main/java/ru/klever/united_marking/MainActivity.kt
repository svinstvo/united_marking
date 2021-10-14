package ru.klever.united_marking



import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import ru.klever.united_marking.add_scanned_code.AddCodeMain
import ru.klever.united_marking.add_km_to_reprint_pool.AddKmToReprintPool
import ru.klever.united_marking.code_viewer.CodeViewerMain
import ru.klever.united_marking.laboratory.LaboratoryMain

class MainActivity : AppCompatActivity() {
    val TAG = "debuu"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val settings: Settings = Settings(this)
        progressBar.visibility = View.INVISIBLE
        val loadstatus: MutableLiveData<Boolean> = MutableLiveData()


        loadstatus.observe(this, Observer { it ->
            if (it) {
                progressBar.visibility = View.VISIBLE
                load_settings_main.visibility = View.INVISIBLE
            } else {
                progressBar.visibility = View.INVISIBLE
                load_settings_main.visibility = View.VISIBLE
                Log.d(TAG, settings.loadedfromserver.toString())
                if (settings.loadedfromserver) {
                    startActivity(settings)
                } else {
                    Log.d(TAG, "not loaded")
                    last_message.text = "Не получилось получить настройки с сервера"
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
        val roles = settings.getRoles()
        for (i in 0 until roles.length()) {
            createButtonDynamically(roles.getString(i))
        }


    }

    private fun createButtonDynamically(text: String) {
        // creating the button
        val dynamicButton = Button(this)
        // setting layout_width and layout_height using layout parameters
        dynamicButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        when (text) {
            "add_code" -> dynamicButton.text = "Ручное добавление кодов в партию"
            "code_viewer" -> dynamicButton.text = "Проверка нанесенных КМ"
            "laboratory" -> dynamicButton.text = "Лаборатория"
            "add_km_to_reprint_pool" -> dynamicButton.text = "Подготовка к перепечатке КМ"
        }
        //dynamicButton.text = text
        dynamicButton.textSize = 24F
        dynamicButton.bottom = 12
        dynamicButton.setOnClickListener { callArm(text) }
        dynamicButton.setBackgroundColor(Color.GREEN)
        // add Button to LinearLayout
        main_activity_linearLayout.addView(dynamicButton)

        val space = Space(this)
        space.minimumHeight = 12
        main_activity_linearLayout.addView(space)


    }

    private fun callArm(text: String) {
        Log.d(TAG, text)
        when (text) {
            "laboratory" -> {
                val intent = Intent(this, LaboratoryMain::class.java)
                startActivity(intent)
            }

            "code_viewer" -> {
                val intent = Intent(this, CodeViewerMain::class.java)
                startActivity(intent)
            }
            "add_code" -> {
                val intent = Intent(this, AddCodeMain::class.java)
                startActivity(intent)
            }
            "add_km_to_reprint_pool" -> {
                val intent=Intent(this,AddKmToReprintPool::class.java)
                startActivity(intent)
            }
        }
    }
}