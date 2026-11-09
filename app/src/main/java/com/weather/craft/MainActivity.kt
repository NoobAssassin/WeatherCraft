package com.weather.craft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weather.craft.data.WeatherResponse
import com.weather.craft.ui.WeatherUiState
import com.weather.craft.ui.WeatherViewModel
import com.weather.craft.util.getWeatherCondition

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
    ) {
        when (val s = state) {
            is WeatherUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is WeatherUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = s.message, color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchWeatherData(28.6139, 77.2090, "New Delhi") }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
            is WeatherUiState.Success -> {
                WeatherDashboard(s)
            }
        }
    }
}

@Composable
fun WeatherDashboard(state: WeatherUiState.Success) {
    val current = state.weather.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(16.dp)) }

        // City & Main Temp
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.cityName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${current?.temperature ?: "--"}°C",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = Color.White
                )
                Text(
                    text = getWeatherCondition(current?.weatherCode ?: 0),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Metrics Grid (Humidity, Wind, Pressure)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Wind",
                    value = "${current?.windSpeed ?: 0} km/h",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Humidity",
                    value = "${current?.humidity ?: 0}%",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "US AQI",
                    value = "${state.aqi.current?.usAqi ?: "--"}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Hourly Carousel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "HOURLY FORECAST",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(12.dp))
                    val hours = state.weather.hourly?.time?.take(24) ?: emptyList()
                    val temps = state.weather.hourly?.temperature2m?.take(24) ?: emptyList()
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        itemsIndexed(hours) { i, time ->
                            val cleanHour = time.takeLast(5)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(cleanHour, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                Spacer(Modifier.height(6.dp))
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFFD54F))
                                Spacer(Modifier.height(6.dp))
                                Text("${temps.getOrNull(i) ?: "--"}°", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 7-Day Forecast
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "7-DAY FORECAST",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    val days = state.weather.daily?.time ?: emptyList()
                    val maxs = state.weather.daily?.tempMax ?: emptyList()
                    val mins = state.weather.daily?.tempMin ?: emptyList()

                    days.forEachIndexed { idx, day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(day, color = Color.White, modifier = Modifier.width(90.dp))
                            Text(
                                getWeatherCondition(state.weather.daily?.weatherCode?.getOrNull(idx) ?: 0),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                            Text(
                                "${mins.getOrNull(idx)}° / ${maxs.getOrNull(idx)}°",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
