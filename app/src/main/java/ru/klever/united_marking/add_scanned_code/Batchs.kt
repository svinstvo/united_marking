package ru.klever.united_marking.add_scanned_code

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.*
import ru.klever.united_marking.Settings
import ru.klever.united_marking.TAG
import java.io.IOException


class Batchs() : ArrayList<batchsItem>() {


    suspend fun getDataFromServerByOrderName(context: Context, requestDate: String): Batchs {
        val client = OkHttpClient()
        val settings: Settings = Settings(context)
        val url =
            settings.getAPIUrl() + "/markirovka/asc/batch/get_by_order_name?order_name=$requestDate"
        var batchs: Batchs

        val request = Request.Builder()
            .url(url)
            .build()
        Log.d(TAG, url)


        val job = GlobalScope.async {
            var body: String = ""
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")


                for ((name, value) in response.headers) {
                    println("$name: $value")
                }
                body = response.body!!.string()
                response.body!!.close()
            }

            return@async body
        }
        val bodyResponce=job.await()
        val gson=Gson()
        batchs = gson.fromJson(bodyResponce,Batchs::class.java)
        return batchs
    }
}



