package com.example.weather

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.example.weather.utils.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class WeatherActivity : AppCompatActivity() {
    lateinit var cityTV: TextView
    lateinit var windTV: TextView
    lateinit var tempTV: TextView
    lateinit var vlazhTV: TextView
    lateinit var maxminTV: TextView
    lateinit var cityET: EditText
    lateinit var getCityBTN: AppCompatButton
    lateinit var getGpsBTN: AppCompatButton
    lateinit var weatherIV: ImageView
    lateinit var mainTB: Toolbar
    private var isLocationRequest = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    @SuppressLint("MissingInflatedId", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weather)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        getCityBTN.setOnClickListener {
            if (cityET.text.isBlank()) {
                clearWeatherStroke()
                return@setOnClickListener
            }
            cityTV.text = cityET.text
            cityET.text.clear()
            isLocationRequest = false
            getWeatherByCity(cityTV.text.toString())
        }
        getGpsBTN.setOnClickListener {
            isLocationRequest = true
            getLocationAndWeather()
        }

    }

    private fun clearWeatherStroke() {
        cityTV.text = ""
        windTV.text = ""
        tempTV.text = ""
        vlazhTV.text = ""
        maxminTV.text = ""
        return
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешения получены", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Разрешения не получены", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getWeatherByCity(city: String) {
        if (isLocationRequest) return
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                clearWeatherStroke()
            }
            val response = try {
                RetrofitInstance.api.getCurrentWeather(
                    city,
                    "metric",
                    getString(R.string.api_key)
                )
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Ошибка подключения: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Ошибка HTTP: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val data = response.body()
                    cityTV.text="${data!!.name}"
                    tempTV.text = "${"%.0f".format(data!!.main.temp)}°C"
                    windTV.text = "Ветер ${"%.1f".format(data!!.wind.speed)} м/с"
                    vlazhTV.text = "Влажность ${data!!.main.humidity} %"
                    maxminTV.text =
                        "Макс ${"%.0f".format(data!!.main.temp_max)}°C/Мин ${"%.0f".format(data!!.main.temp_min)}°C "

                    val iconID = data.weather[0].icon
                    val imageUrl = "https://openweathermap.org/img/wn/${iconID}@2x.png"
                    weatherIV.let {
                        Glide.with(this@WeatherActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_placeholder)
                            .error(R.drawable.ic_error)
                            .into(it)
                    }
                }
                isLocationRequest = false
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Ошибка сервера: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getWeatherByLocation(lat: Double, lon: Double) {
        if (!isLocationRequest) return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    getWeatherByLocation(lat, lon)
                } else {
                    Toast.makeText(this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Ошибка получения местоположения: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Необходимо разрешение для доступа к местоположению", Toast.LENGTH_SHORT).show()
        }
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.api.getCurrentWeatherByCoords(
                    lat,
                    lon,
                    "metric",
                    getString(R.string.api_key)
                )
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Ошибка подключения: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                return@launch
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Ошибка HTTP: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val data = response.body()
                    cityTV.text="${data!!.name}"
                    tempTV.text = "${"%.0f".format(data!!.main.temp)}°C"
                    windTV.text = "Ветер ${"%.1f".format(data.wind.speed)} м/с"
                    vlazhTV.text = "Влажность ${data.main.humidity} %"
                    maxminTV.text = "Макс ${"%.0f".format(data.main.temp_max)}°C/Мин ${"%.0f".format(data.main.temp_min)}°C"
                    val iconID = data.weather[0].icon
                    val imageUrl = "https://openweathermap.org/img/wn/${iconID}@2x.png"
                    Glide.with(this@WeatherActivity)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(weatherIV)
                }
                isLocationRequest = false
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Ошибка сервера: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLocationAndWeather() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                getWeatherByLocation(lat, lon)
            } else {
                Toast.makeText(this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка получения местоположения: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun init() {
        cityTV = findViewById(R.id.cityTV)
        windTV = findViewById(R.id.windTV)
        tempTV = findViewById(R.id.tempTV)
        vlazhTV = findViewById(R.id.vlazhTV)
        maxminTV = findViewById(R.id.maxminTV)
        cityET = findViewById(R.id.cityET)
        getCityBTN = findViewById(R.id.getCityBTN)
        getGpsBTN = findViewById(R.id.getGpsBTN)
        weatherIV = findViewById(R.id.weatherIV)
        mainTB = findViewById(R.id.mainTB)
        setSupportActionBar(mainTB)
        title = "Погода"
        mainTB.setTitleTextColor(getColor(R.color.white))
        mainTB.subtitle = "version 1.0"
        mainTB.setLogo(R.drawable.baseline_sunny_24)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.exit->{
                finishAffinity()
                Toast.makeText(this,"Программа завершена", Toast.LENGTH_SHORT).show()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }
}