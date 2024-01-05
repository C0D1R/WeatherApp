package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import java.text.SimpleDateFormat
import java.util.Locale

class TenDayWeatherForecast : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                getTenDayWeatherForecastData("臺北市")
                TenDaysForecastScreen(onBackClick = { finish() })
            }
        }
    }
}

data class TenDayWeatherForecastData(
    val time: String,
    val weather: String,
    val maxTemperature: String,
    val minTemperature: String,
)

//  1. Get Data
//      (1) send request
//      (2) return response
//  2. Parse Data
//      (1) use response
//      (2) return parse data
//  3. Generate UI
//      (1) use parse data
//      (2) add Card Lists

@Composable
fun getTenDayWeatherForecastData(location: String) {
    suspend fun fetchData(url: String): String {
        val weatherRequest = WeatherRequest()
        val authorizationToken = "CWA-697924D3-80DE-4412-98E2-640E4DB6A31C"

        val data = withContext(Dispatchers.IO) {
            weatherRequest.sendGetRequest(url, authorizationToken)
        }

        return data
    }

    val futureOneWeekCityNumberMap = mapOf(
        "基隆市" to "051",
        "台北市" to "063",
        "新北市" to "071",
        "桃園市" to "007",
        "新竹市" to "055",
        "新竹縣" to "011",
        "宜蘭縣" to "003",

        "苗栗縣" to "015",
        "台中市" to "075",
        "彰化縣" to "019",
        "南投縣" to "023",
        "雲林縣" to "027",

        "嘉義市" to "059",
        "嘉義縣" to "031",
        "台南市" to "079",
        "高雄市" to "067",
        "屏東縣" to "035",

        "花蓮縣" to "043",
        "台東縣" to "039",

        "澎湖縣" to "047",
        "連江縣" to "083",
        "金門縣" to "087",
    )

    var weatherDataList by remember {
        mutableStateOf<MutableList<TenDayWeatherForecastData>>(mutableListOf())
    }

    val cityNumber = futureOneWeekCityNumberMap[location]
    LaunchedEffect(Unit) {
        val oneWeekWeatherForecastData = fetchData("https://opendata.cwa.gov.tw/fileapi/v1/opendataapi/F-C0032-003?Authorization=CWA-697924D3-80DE-4412-98E2-640E4DB6A31C&format=JSON&locationName=新北市&elementName=Wx,MinT,MaxT")

        val resultDataList = parseTenDayWeatherForecastData(oneWeekWeatherForecastData, location)

        println(resultDataList)
        weatherDataList = resultDataList
    }

    generateTenDayWeatherForecastUI(weatherDataList, location)
}

fun parseTenDayWeatherForecastData(data: String, cityName: String): MutableList<TenDayWeatherForecastData> {
    val tenDayWeatherForecastDataList = mutableListOf<TenDayWeatherForecastData>()

    val jsonObject = JSONObject(data)
    val locationArray = jsonObject
        .getJSONObject("cwaopendata")
        .getJSONObject("dataset")
        .getJSONArray("location")

    for (i in 0 until locationArray.length()) {
        val locationItem = locationArray.getJSONObject(i)

        if (cityName == locationItem.optString("locationName")) {
            val weatherElement = locationItem.getJSONArray("weatherElement")

            val wxArray = weatherElement
                .getJSONObject(0)
                .getJSONArray("time")
            val maxTArray = weatherElement
                .getJSONObject(1)
                .getJSONArray("time")
            val minTArray = weatherElement
                .getJSONObject(2)
                .getJSONArray("time")

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

            for (j in 0 until wxArray.length()) {
                val time = wxArray
                    .getJSONObject(j)
                    .optString("startTime")
                val weather = wxArray
                    .getJSONObject(j)
                    .getJSONObject("parameter")
                    .optString("parameterName")
                val maxT = maxTArray
                    .getJSONObject(j)
                    .getJSONObject("parameter")
                    .optString("parameterName")
                val minT = minTArray
                    .getJSONObject(j)
                    .getJSONObject("parameter")
                    .optString("parameterName")

                tenDayWeatherForecastDataList.add(
                    TenDayWeatherForecastData(
                        outputFormat.format(inputFormat.parse(time)),
                        weather,
                        "最高$maxT°C",
                        "最低$minT°C"
                    )
                )
            }
        }
    }

    return tenDayWeatherForecastDataList
}

@Composable
fun generateTenDayWeatherForecastUI(weatherDataList: MutableList<TenDayWeatherForecastData>, location: String) {
    weatherDataList?.let { dataList ->
        Card(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "$location 10天天氣預報",
                style = MaterialTheme.typography.headlineSmall,
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
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = data.time,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = data.weather,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            Text(
                                text = data.minTemperature,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = data.maxTemperature,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TenDaysForecastScreen(onBackClick: () -> Unit) {
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
fun TenDayWeatherForecastPreview() {
    WeatherAppTheme {
        getTenDayWeatherForecastData("臺北市")
    }
}