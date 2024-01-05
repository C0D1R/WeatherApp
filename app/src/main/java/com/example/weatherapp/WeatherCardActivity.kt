package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Objects
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherCardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                WeatherCard()
                HourlyForecastScreen(onBackClick = { finish() })
            }
        }
    }
}

val cityNumberMap = mapOf(
    "基隆市" to "049",
    "台北市" to "061",
    "新北市" to "069",
    "桃園市" to "005",
    "新竹市" to "053",
    "新竹縣" to "009",
    "宜蘭縣" to "001",

    "苗栗縣" to "013",
    "台中市" to "073",
    "彰化縣" to "017",
    "南投縣" to "021",
    "雲林縣" to "025",

    "嘉義市" to "057",
    "嘉義縣" to "029",
    "台南市" to "077",
    "高雄市" to "065",
    "屏東縣" to "033",

    "花蓮縣" to "041",
    "台東縣" to "037",

    "澎湖縣" to "045",
    "連江縣" to "081",
    "金門縣" to "085",
)

fun getNumberFromCityName(cityName: String): String {
    return cityNumberMap[cityName] ?: "default"
}

data class WeatherData(val time: String, val weatherDescription: String, val temperature: String)

@Composable
fun WeatherCard() {
    var weatherDataList by remember {
        mutableStateOf<MutableList<WeatherData>>(mutableListOf())
    }

    suspend fun fetchData(url: String): String {
        val weatherRequest = WeatherRequest()
        val authorizationToken = "CWA-697924D3-80DE-4412-98E2-640E4DB6A31C"

        val data = withContext(Dispatchers.IO) {
            weatherRequest.sendGetRequest(url, authorizationToken)
        }

        return data
    }

    val cityNameNumber = cityNumberMap["台北市"];
    LaunchedEffect(Unit) {
        val sunTime = fetchData("https://opendata.cwa.gov.tw/api/v1/rest/datastore/A-B0062-001?Authorization=CWA-697924D3-80DE-4412-98E2-640E4DB6A31C&format=JSON&CountyName=%E8%87%BA%E5%8D%97%E5%B8%82&Date=2024-01-03&parameter=SunRiseTime,SunSetTime")
        val weather = fetchData("https://opendata.cwa.gov.tw/api/v1/rest/datastore/F-D0047-$cityNameNumber?Authorization=CWA-697924D3-80DE-4412-98E2-640E4DB6A31C&elementName=MinT,MaxT,PoP12h,T,Wx,WeatherDescription")

        val resultDataList = parseSunTimeData(sunTime)
        resultDataList.addAll(parseWeatherData(weather))

        weatherDataList = resultDataList
    }

    weatherDataList?.let { dataList ->
        Card(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "台北市",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                itemsIndexed(dataList) { index, data ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = data.time,
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = data.temperature,
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseSunTimeData(data: String): MutableList<WeatherData> {
    val jsonObject = JSONObject(data)
    val sunTime = jsonObject
        .getJSONObject("records")
        .getJSONObject("locations")
        .getJSONArray("location")
        .getJSONObject(0)
        .getJSONArray("time")
        .getJSONObject(0)
    val sunRiseTime = sunTime.optString("SunRiseTime")
    val sunSetTime = sunTime.optString("SunSetTime")

    val weatherDataList = mutableListOf<WeatherData>()

    val sunRiseTimeObject = WeatherData(sunRiseTime, "", "日出")
    val sunSetTimeObject = WeatherData(sunSetTime, "", "日落")

    weatherDataList.add(sunRiseTimeObject)
    weatherDataList.add(sunSetTimeObject)

    return weatherDataList
}

fun parseWeatherData(data: String): MutableList<WeatherData> {
    val weatherDataList = mutableListOf<WeatherData>()

    val jsonObject = JSONObject(data)
    val locationObject  = jsonObject
        .getJSONObject("records")
        .getJSONArray("locations")
        .getJSONObject(0)

    val locationsName = locationObject.optString("locationsName")
    val locationArray = locationObject.getJSONArray("location")

    val weatherElementObject = locationArray.getJSONObject(0)
    val locationName = weatherElementObject.optString("locationName")

    val weatherArray = weatherElementObject.getJSONArray("weatherElement")
    val temperatureObject = weatherArray.getJSONObject(1)

    val timeArray = temperatureObject.getJSONArray("time")
    for (i in 0 until timeArray.length()) {
        val timeObject = timeArray.getJSONObject(i)

        val startTime = timeObject.optString("startTime")

        val elementObject = timeObject.getJSONArray("elementValue")
        val valueObject = elementObject.getJSONObject(0)

        val value = valueObject.optString("value")

        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

        val date = inputDateFormat.parse(startTime)
        val formattedDate = outputDateFormat.format(date)

        val weatherObject = WeatherData("${formattedDate}時", "", "$value°C")
        weatherDataList.add(weatherObject)
    }

    return weatherDataList
}

@Composable
fun HourlyForecastScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            modifier = Modifier
                .padding(8.dp),
            onClick = {
                onBackClick()
            }
        ) {
            Text(
                text = "回主頁面",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherCardPreview() {
    WeatherAppTheme {
        WeatherCard()
    }
}