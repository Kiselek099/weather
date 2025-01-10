package com.example.weather.utils


import com.example.weather.models.CurrentWeather
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET ("weather?")
    suspend fun getCurrentWeather(
    @Query ("q") city:String,
    @Query ("units")units :String,
    @Query ("appid") apiKey :String
    ): retrofit2.Response<CurrentWeather>
    @GET("weather?")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): retrofit2.Response<CurrentWeather>
}