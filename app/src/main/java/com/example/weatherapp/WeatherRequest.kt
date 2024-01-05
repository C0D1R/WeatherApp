package com.example.weatherapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class WeatherRequest {
    fun sendGetRequest(urlString: String, authorizationToken: String): String {
        val response = StringBuilder()

        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            connection.setRequestProperty("Authorization", authorizationToken)

            val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
            var inputLine: String?

            while (inputStream.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            inputStream.close()
        }
        catch (e: Exception) {
            e.printStackTrace()
            Log.e("WeatherApp", "Exception: ${e.message}")
        }

        return response.toString()
    }
}