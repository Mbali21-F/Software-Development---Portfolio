package com.example.weather;

import android.os.*;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.*;

import org.json.*;

import java.io.IOException;
import java.util.concurrent.*;

import okhttp3.*;


public class MainActivity extends AppCompatActivity {

    private TextView cityNameText, tempText, humidText, descripText, winText;
    private ImageView weatIcon;
    private Button refreshButton;
    private EditText cityNamInput;
    private static final String API_KEY = "b67e480be06907b0d8a9dfe5bae46feb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);


        cityNameText = findViewById(R.id.cityName);
        tempText = findViewById(R.id.temperature);
        humidText = findViewById(R.id.humidityText);
        descripText = findViewById(R.id.descriptionText);
        winText = findViewById(R.id.windText);

        weatIcon = findViewById(R.id.weatherIcon);
        refreshButton = findViewById(R.id.fetchWeatherBtn);
        cityNamInput = findViewById(R.id.cityNameInput);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String cityName = cityNamInput.getText().toString().trim();

                if (!cityName.isEmpty()) {
                    FetchWeatherData(cityName);
                } else {
                    cityNamInput.setError("Please enter a city name");
                }
            }
        });

        FetchWeatherData("Cape Town");
    }

    private void FetchWeatherData(String cityName) {
        HttpUrl url = HttpUrl.parse("https://api.openweathermap.org/data/2.5/weather")
                .newBuilder()
                .addQueryParameter("q", cityName)
                .addQueryParameter("appid", API_KEY)
                .addQueryParameter("units", "metric")
                .build();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() ->
                {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();

                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful() && response.body() != null) {
                            String result = response.body().string();
                            runOnUiThread(() -> updateUI(result));
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "City not found or request failed", Toast.LENGTH_SHORT).show());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show());
                    }

                }
        );

    }

    private void updateUI(String result) {

        if (result != null) {

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                double temperature = main.getDouble("temp");
                double humidity = main.getDouble("humidity");
                double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");

                JSONObject weatherObj = jsonObject.getJSONArray("weather").getJSONObject(0);
                String mainCondition = weatherObj.optString("main", "");
                String description = weatherObj.getString("description");
                String iconCode = weatherObj.getString("icon");

                setWeatherIcon(iconCode, mainCondition);

                cityNameText.setText(jsonObject.getString("name"));
                tempText.setText(String.format("%.0f\u00B0C", temperature));
                humidText.setText(String.format("%.0f%%", humidity));
                winText.setText(String.format("%.0f km/h", windSpeed));
                descripText.setText(description);


            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void setWeatherIcon(String iconCode, String mainCondition) {
        int resId = getResources().getIdentifier("ic_" + iconCode, "drawable", getPackageName());
        if (resId == 0 && iconCode != null) {
            switch (iconCode) {
                case "01d":
                    resId = R.drawable.sun;
                    break;
                case "01n":
                    resId = R.drawable.moon_and_stars;
                    break;
                case "02d":
                    resId = R.drawable.sun;
                    break;
                case "02n":
                    resId = R.drawable.partly_cloud_night;
                    break;
                case "03d":
                case "03n":
                case "04d":
                case "04n":
                    resId = R.drawable.cloudy;
                    break;
                case "09d":
                case "09n":
                case "10d":
                case "10n":
                    resId = R.drawable.heavy_rain;
                    break;
                case "11d":
                case "11n":
                    resId = R.drawable.thunder;
                    break;
                case "13d":
                case "13n":
                    resId = R.drawable.snow;
                    break;
                case "50d":
                case "50n":
                    resId = R.drawable.mist;
                    break;
            }
        }
        if (resId == 0 && mainCondition != null) {
            switch (mainCondition.toLowerCase()) {
                case "clear":
                    resId = R.drawable.clear_sky;
                    break;
                case "clouds":
                    resId = R.drawable.cloudy;
                    break;
                case "rain":
                case "drizzle":
                    resId = R.drawable.heavy_rain;
                    break;
                case "thunderstorm":
                    resId = R.drawable.thunder;
                    break;
                case "snow":
                    resId = R.drawable.snow;
                    break;
                case "mist":
                case "fog":
                case "haze":
                    resId = R.drawable.foggy;
                    break;
            }
        }
        if (resId != 0) {
            weatIcon.setImageResource(resId);
        }
    }
}