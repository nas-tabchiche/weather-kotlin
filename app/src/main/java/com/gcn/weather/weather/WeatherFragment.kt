package com.gcn.weather.weather

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.gcn.weather.App
import com.gcn.weather.R
import com.gcn.weather.openweathermap.WeatherWrapper
import com.gcn.weather.openweathermap.mapOpenWeatherDataToWeather
import com.gcn.weather.utils.toast
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherFragment: Fragment() {

    private val TAG = WeatherFragment::class.java.simpleName

    private lateinit var cityName: String


    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var city: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var weatherDescription: TextView
    private lateinit var temperature: TextView
    private lateinit var humidity: TextView
    private lateinit var pressure: TextView



    companion object {
        val EXTRA_CITY_NAME = "com.gcn.weather.extras.EXTRA_CITY_NAME"
        fun newInstance() = WeatherFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)

        refreshLayout = view.findViewById(R.id.swipe_refresh)

        city = view.findViewById(R.id.city)
        weatherIcon = view.findViewById(R.id.weather_icon)
        weatherDescription = view.findViewById(R.id.weather_description)
        temperature = view.findViewById(R.id.temperature)
        humidity = view.findViewById(R.id.humidity)
        pressure = view.findViewById(R.id.pressure)

        refreshLayout.setOnRefreshListener { refreshWeather() }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity?.intent!!.hasExtra(EXTRA_CITY_NAME)) {
            updateWeatherForCity(activity!!.intent.getStringExtra(EXTRA_CITY_NAME)!!)
        }
    }

    fun updateWeatherForCity(cityName: String) {
        this.cityName = cityName
        this.city.text = cityName

        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }

        val call = App.weatherService.getWeather("$cityName,fr")
        call.enqueue(object: Callback<WeatherWrapper> {
            override fun onResponse(call: Call<WeatherWrapper>, response: Response<WeatherWrapper>) {
                response.body()?.let {
                    val weather = mapOpenWeatherDataToWeather(it)
                    updateUi(weather)
                    Log.i(TAG, "OpenWeatherMap response: $weather")
                    refreshLayout.isRefreshing = false

                }
            }

            override fun onFailure(call: Call<WeatherWrapper>, t: Throwable) {
                Log.e(TAG, getString(R.string.weather_message_error_could_not_load_city_weather), t)
                context?.toast(getString(R.string.weather_message_error_could_not_load_city_weather))
                refreshLayout.isRefreshing = false
            }

        })
    }

    private fun updateUi(weather: Weather) {
        weatherDescription.text = weather.description
        temperature.text = getString(R.string.weather_temperature_value, weather.temperature.toInt())
        humidity.text = getString(R.string.weather_humidity_value, weather.humidity)
        pressure.text = getString(R.string.weather_pressure_value, weather.pressure)

        Picasso.get()
            .load(weather.iconUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(weatherIcon)
    }

    private fun refreshWeather() {
        updateWeatherForCity(cityName)
    }

    fun clearUi() {
        weatherIcon.setImageResource(R.drawable.ic_launcher_background)
        cityName = ""
        city.text = ""
        weatherDescription.text = ""
        temperature.text = ""
        humidity.text = ""
        pressure.text = ""
    }
}