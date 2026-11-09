package com.weather.craft.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.craft.data.AirQualityResponse
import com.weather.craft.data.NetworkClient
import com.weather.craft.data.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(
        val weather: WeatherResponse,
        val aqi: AirQualityResponse,
        val cityName: String
    ) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // Defaults to New Delhi (28.6139, 77.2090)
        fetchWeatherData(28.6139, 77.2090, "New Delhi, India")
    }

    fun fetchWeatherData(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val forecast = NetworkClient.weatherApi.getForecast(lat, lon)
                val aqi = NetworkClient.aqiApi.getAqi(lat, lon)
                _uiState.value = WeatherUiState.Success(forecast, aqi, name)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.localizedMessage ?: "Failed to connect to weather services.")
            }
        }
    }
}
