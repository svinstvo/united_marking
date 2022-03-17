package ru.klever.united_marking



import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.dcastalia.localappupdate.DownloadApk
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.klever.united_marking.add_km_to_reprint_pool.AddKmToReprintPool
import ru.klever.united_marking.add_scanned_code.AddCodeMain
import ru.klever.united_marking.code_viewer.CodeViewerMain
import ru.klever.united_marking.dropout.DropoutMain
import ru.klever.united_marking.remove.RemoveMain
import java.lang.Exception
import android.os.CountDownTimer as CountDownTimer


class MainActivity : AppCompatActivity() {
    val TAG = "debuu"
    lateinit var settings: Settings
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main)
        settings = Settings(this)
        progressBar.visibility = View.INVISIBLE
        val loadstatus: MutableLiveData<Boolean> = MutableLiveData()

        checkUpdate()
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
                    last_message.text=""
                    load_settings_main.visibility=View.INVISIBLE
                } else {
                    Log.d(TAG, "not loaded")
                    last_message.text = "Не получилось получить настройки с сервера"
                    load_settings_main.visibility=View.VISIBLE
                    startCounter(loadstatus)
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

    private fun startCounter(loadstatus:MutableLiveData<Boolean>) {
        val timer= object : CountDownTimer(5000,1000) {
            override fun onTick(milliseconds: Long) {
                load_settings_main.text="не удалось получить настройки повтор через ${(milliseconds/1000)}"
            }

            override fun onFinish() {
                settings.loadSetingsMain(loadstatus)
            }

        }.start()
    }

    private fun checkUpdate() {
        val updateVersion=MutableLiveData<String>()
        updateVersion.observe(this,{
            Log.d(TAG,it)
        })
        GlobalScope.launch {
            try{
                val url=settings.settingsUrl+"/currentversion"
                Log.d(TAG,url)
                val r= khttp.get(url)
                val versionCode = BuildConfig.VERSION_CODE
                if (versionCode.toString() != r.text) {

                    Handler(Looper.getMainLooper()).post {
                        val update_url=settings.getUpdatePath()
                        Log.d(TAG,update_url)
                        val downloadApk = DownloadApk(this@MainActivity)
                        downloadApk.startDownloadingApk(update_url,"unitedmarking")
                    }
                } else {
                    Log.d(TAG,"апдейт не нужен")
                }
            } catch (e:Exception) {
                Log.d(TAG,e.localizedMessage.toString())
            }
        }
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
            "add_code" -> dynamicButton.text = "Ручное добавление кодов"
            "code_viewer" -> dynamicButton.text = "Проверка КМ"
            "dropout" -> dynamicButton.text = "Выбытие введеных КМ"
            "remove" -> dynamicButton.text = "Выбытие НЕ введеных КМ"
            "add_km_to_reprint_pool" -> dynamicButton.text = "Перепечатка КМ"
            "none" -> dynamicButton.text="Терминалу ${settings.id} не назначена роль"
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
            "dropout" -> {
                val intent = Intent(this, DropoutMain::class.java)
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
            "remove" -> {
                val intent=Intent(this,RemoveMain::class.java)
                startActivity(intent)
            }
        }
    }
}