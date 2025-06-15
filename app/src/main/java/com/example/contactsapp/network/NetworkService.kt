package com.example.contactsapp.network


import android.util.Log
import com.example.contactsapp.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkService {

    private val logging = HttpLoggingInterceptor { message ->
        try {
            if (message.startsWith("{")) {
                // JSON Object
                val prettyJson = JSONObject(message).toString(4)
                Log.d("OkHttp", prettyJson)
            } else if (message.startsWith("[")) {
                // JSON Array
                val prettyJson = JSONArray(message).toString(4)
                Log.d("OkHttp", prettyJson)
            } else {
                Log.d("OkHttp", message)
            }
        } catch (e: Exception) {
            Log.d("OkHttp", message) // fallback to raw message
        }
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(50, TimeUnit.SECONDS)
        .writeTimeout(50, TimeUnit.SECONDS)
        .callTimeout(50,TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // ← attaching the custom OkHttpClient
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val networkInterface = retrofit.create(NetworkServiceInterface::class.java)

}
