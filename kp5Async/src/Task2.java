import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Task2 {

    public static void main(String[] args) {
        CompletableFuture<Weather> city1Weather = fetchWeather("City 1");
        CompletableFuture<Weather> city2Weather = fetchWeather("City 2");
        CompletableFuture<Weather> city3Weather = fetchWeather("City 3");

        CompletableFuture<Object> anyWeatherData = CompletableFuture.anyOf(city1Weather, city2Weather, city3Weather);

        anyWeatherData.thenAccept(weather -> {
            System.out.println("First weather data received: " + weather);
        });

        CompletableFuture<Void> allWeatherData = CompletableFuture.allOf(city1Weather, city2Weather, city3Weather);

        allWeatherData.thenRun(() -> {
            try {
                Weather w1 = city1Weather.get();
                Weather w2 = city2Weather.get();
                Weather w3 = city3Weather.get();

                System.out.println("Weather Comparison:");
                System.out.println(w1);
                System.out.println(w2);
                System.out.println(w3);

                if (w1.isGoodForBeach()) {
                    System.out.println(w1.city + " is good for the beach");
                }
                if (w2.isGoodForBeach()) {
                    System.out.println(w2.city + " is good for the beach");
                }
                if (w3.isGoodForBeach()) {
                    System.out.println(w3.city + " is good for the beach");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



        sleep(5000);
    }

    private static CompletableFuture<Weather> fetchWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching weather data for " + city);
            simulateDelay();
            int temperature = ThreadLocalRandom.current().nextInt(15, 40);
            int humidity = ThreadLocalRandom.current().nextInt(20, 80);
            int windSpeed = ThreadLocalRandom.current().nextInt(0, 20);
            System.out.println("Weather data fetched for " + city);
            return new Weather(city, temperature, humidity, windSpeed);
        });
    }

    private static void simulateDelay() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }//

    static class Weather {
        String city;
        int temperature;
        int humidity;
        int windSpeed;

        public Weather(String city, int temperature, int humidity, int windSpeed) {
            this.city = city;
            this.temperature = temperature;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
        }

        public boolean isGoodForBeach() {
            return temperature > 25 && humidity < 60 && windSpeed < 10;
        }

        @Override
        public String toString() {
            return city + " -> Temperature: " + temperature + "C, Humidity: " + humidity + "%, Wind Speed: " + windSpeed + " km/h";
        }
    }
}

